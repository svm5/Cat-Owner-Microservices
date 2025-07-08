package labs.externalmicroservice;

import labs.GetAllOwnersResponse;
import labs.GetOwnerDTO;
import labs.OwnerDTO;
import labs.externalmicroservice.security.AuthEntryPointJwt;
import labs.externalmicroservice.security.SecurityConfig;
import labs.externalmicroservice.security.jwt.JwtService;
import labs.externalmicroservice.security.jwt.JwtTokenFilter;
import labs.externalmicroservice.service.OwnerService;
import labs.externalmicroservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import labs.externalmicroservice.presentation.OwnerController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OwnerController.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = true)
@EnableMethodSecurity(prePostEnabled = true)
@EnableAsync
@Import({ SecurityConfig.class, JwtTokenFilter.class, AuthEntryPointJwt.class })
public class OwnerControllerTests {
    @Autowired
    private WebApplicationContext context;

    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    OwnerService ownerService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserDetailsService userDetailsService;

    @Autowired
    AuthEntryPointJwt authEntryPointJwt;

    @Autowired
    JwtTokenFilter jwtTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(result -> result.getRequest().setAsyncSupported(true))
                .addFilter(jwtTokenFilter)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    public void getOwnerSuccessfullyTest() throws Exception {
        OwnerDTO ownerDTO = new OwnerDTO(Long.valueOf(1), "owner1", LocalDate.parse("2010-10-10"), List.of((long)1, (long)2));
        GetOwnerDTO getOwnerDTO = new GetOwnerDTO(ownerDTO, "");
        Mockito.when(ownerService.getOwner(any())).thenReturn(getOwnerDTO);
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(true);
        Mockito.when(userService.checkAdmin()).thenReturn(false);

        MvcResult asyncResult = mockMvc.perform(get("/owners/{id}", 1L))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(getOwnerDTO.ownerDTO)));

        Mockito.verify(ownerService, Mockito.times(1)).getOwner(any());
        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
        Mockito.verify(userService, Mockito.times(0)).checkAdmin();
    }

    @Test
    @WithMockUser
    public void getOwnerHandleOwnerNotFoundTest() throws Exception {
        Mockito.when(ownerService.getOwner(any())).thenReturn(new GetOwnerDTO(null, "smth went wrong"));
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(true);
        Mockito.when(userService.checkAdmin()).thenReturn(false);

        MvcResult asyncResult = mockMvc.perform(get("/owners/{id}", 1L))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNotFound());
        Mockito.verify(ownerService, Mockito.times(1)).getOwner(any());
        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
        Mockito.verify(userService, Mockito.times(0)).checkAdmin();
    }

    @Test
    @WithMockUser
    public void getOwnerByAdminHandleOwnerNotFoundTest() throws Exception {
        Mockito.when(ownerService.getOwner(any())).thenReturn(new GetOwnerDTO(null, "smth went wrong"));
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(false);
        Mockito.when(userService.checkAdmin()).thenReturn(true);

        MvcResult asyncResult = mockMvc.perform(get("/owners/{id}", 1L))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNotFound());

        Mockito.verify(ownerService, Mockito.times(1)).getOwner(any());
        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }

    @Test
    @WithMockUser
    public void getOwnerHandleForbidden() throws Exception {
        Mockito.when(ownerService.getOwner(any())).thenReturn(new GetOwnerDTO(null, "smth went wrong"));
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(false);
        Mockito.when(userService.checkAdmin()).thenReturn(false);

        MvcResult asyncResult = mockMvc.perform(get("/owners/{id}", 1L))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());

        Mockito.verify(ownerService, Mockito.times(0)).getOwner(any());
        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }

    @Test
    @WithAnonymousUser
    public void getOwnerHandleUnauthorized() throws Exception {
        mockMvc.perform(get("/owners/{id}", 1L)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void getAllOwnersHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/owners")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getAllOwnersByAdminTest() throws Exception {
        OwnerDTO ownerDTO1 = new OwnerDTO(Long.valueOf(100), "owner1", LocalDate.parse("2010-10-10"), List.of((long)500));
        OwnerDTO ownerDTO2 = new OwnerDTO(Long.valueOf(101), "owner2", LocalDate.parse("2010-10-10"), List.of((long)501));

        GetAllOwnersResponse getAllOwnersResponse = new GetAllOwnersResponse(List.of(ownerDTO1, ownerDTO2));
        Mockito.when(ownerService.getAllOwners(any())).thenReturn(getAllOwnersResponse);
        Mockito.when(userService.checkAdmin()).thenReturn(true);

        MvcResult asyncResult = mockMvc.perform(get("/owners"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(getAllOwnersResponse.owners)));
        Mockito.verify(ownerService, Mockito.times(1)).getAllOwners(any());
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }

    @Test
    @WithMockUser
    public void getAllOwnersWithCriteriaTest() throws Exception {
        OwnerDTO ownerDTO1 = new OwnerDTO(Long.valueOf(100), "owner1", LocalDate.parse("2010-10-10"), List.of((long)500));
        OwnerDTO ownerDTO2 = new OwnerDTO(Long.valueOf(101), "owner2", LocalDate.parse("2010-10-10"), List.of((long)501));

        GetAllOwnersResponse getAllOwnersResponse = new GetAllOwnersResponse(List.of(ownerDTO1, ownerDTO2));
        Mockito.when(ownerService.getAllOwners(any())).thenReturn(getAllOwnersResponse);
        Mockito.when(userService.checkAdmin()).thenReturn(true);

        MvcResult asyncResult = mockMvc.perform(
                get("/owners")
                    .param("page", "0")
                    .param("size", "2")
                    .param("birthday", "2010-10-10"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(getAllOwnersResponse.owners)));
        Mockito.verify(ownerService, Mockito.times(1)).getAllOwners(any());
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }

    @Test
    @WithAnonymousUser
    public void deleteOwnerHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(delete("/owners/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void deleteOwnerByAdminSuccessfullyTest() throws Exception {
        Mockito.when(userService.checkAdmin()).thenReturn(true);
        MvcResult asyncResult = mockMvc.perform(delete("/owners/1").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNoContent());

        Mockito.verify(ownerService, Mockito.times(1)).deleteOwnerById(any());
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }

    @Test
    @WithMockUser
    public void deleteOwnerByUserSuccessfullyTest() throws Exception {
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(true);
        MvcResult asyncResult = mockMvc.perform(delete("/owners/1").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNoContent());

        Mockito.verify(ownerService, Mockito.times(1)).deleteOwnerById(any());
        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
    }

    @Test
    @WithMockUser
    public void deleteOwnerHandleForbiddenTest() throws Exception {
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(false);
        Mockito.when(userService.checkAdmin()).thenReturn(false);
        MvcResult asyncResult = mockMvc.perform(delete("/owners/1").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());

        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }


    @Test
    @WithMockUser
    public void deleteAllOwnersByAdminSuccessfullyTest() throws Exception {
        Mockito.doNothing().when(ownerService).deleteAllOwners();
        Mockito.when(userService.checkAdmin()).thenReturn(true);

        mockMvc.perform(delete("/owners").with(csrf()))
                        .andExpect(status().isNoContent());

        Mockito.verify(ownerService, Mockito.times(1)).deleteAllOwners();
        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
    }

    @Test
    @WithMockUser
    public void deleteAllOwnersByUserHandleForbiddenTest() throws Exception {
        mockMvc.perform(delete("/owners").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void deleteAllOwnersHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(delete("/owners").with(csrf())).andExpect(status().isUnauthorized());
    }
}