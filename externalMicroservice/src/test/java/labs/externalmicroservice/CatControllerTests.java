package labs.externalmicroservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import labs.*;
import labs.externalmicroservice.presentation.CatController;
import labs.externalmicroservice.security.AuthEntryPointJwt;
import labs.externalmicroservice.security.SecurityConfig;
import labs.externalmicroservice.security.jwt.JwtService;
import labs.externalmicroservice.security.jwt.JwtTokenFilter;
import labs.externalmicroservice.service.CatService;
import labs.externalmicroservice.service.OwnerService;
import labs.externalmicroservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CatController.class)
@EnableMethodSecurity(prePostEnabled = true)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = true)
@EnableAsync
@Import({ SecurityConfig.class, JwtTokenFilter.class, AuthEntryPointJwt.class })
public class CatControllerTests {
    @Autowired
    private WebApplicationContext context;

    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @MockitoBean
    CatService catService;

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
    public void createCatHandleUnauthorized() throws Exception {
        CreateCatDTO createCatDTO = new CreateCatDTO("cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, "blue", Long.valueOf(1));
        mockMvc.perform(post("/cats")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(createCatDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void createCatByAdminSuccessfullyTest() throws Exception {
        CreateCatDTO createCatDTO = new CreateCatDTO("cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, "blue", Long.valueOf(1));
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        OwnerDTO ownerDTO = new OwnerDTO(Long.valueOf(1), "owner1", LocalDate.parse("2010-10-10"), List.of((long)1, (long)2));
        GetOwnerDTO getOwnerDTO = new GetOwnerDTO(ownerDTO, "");
        Mockito.when(ownerService.getOwner(any())).thenReturn(getOwnerDTO);
        Mockito.when(catService.createCat(any())).thenReturn(catDTO);
        Mockito.when(userService.checkAdmin()).thenReturn(true);

        MvcResult asyncResult = mockMvc.perform(post("/cats")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(createCatDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(catDTO)));

        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
        Mockito.verify(ownerService, Mockito.times(1)).getOwner(any());
        Mockito.verify(catService, Mockito.times(1)).createCat(any());
    }

    @Test
    @WithMockUser
    public void createCatByAdminHandleOwnerNotFoundTest() throws Exception {
        CreateCatDTO createCatDTO = new CreateCatDTO("cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, "blue", Long.valueOf(1));
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetOwnerDTO getOwnerDTO = new GetOwnerDTO(null, "");
        Mockito.when(ownerService.getOwner(any())).thenReturn(getOwnerDTO);
        Mockito.when(catService.createCat(any())).thenReturn(catDTO);
        Mockito.when(userService.checkAdmin()).thenReturn(true);

        MvcResult asyncResult = mockMvc.perform(post("/cats")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(createCatDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).checkAdmin();
        Mockito.verify(ownerService, Mockito.times(1)).getOwner(any());
        Mockito.verify(catService, Mockito.times(0)).createCat(any());
    }

    @Test
    @WithMockUser()
    public void createCatByUserSuccessfullyTest() throws Exception {
        CreateCatDTO createCatDTO = new CreateCatDTO("cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, "blue", Long.valueOf(1));
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        OwnerDTO ownerDTO = new OwnerDTO(Long.valueOf(1), "owner1", LocalDate.parse("2010-10-10"), List.of((long)1, (long)2));
        GetOwnerDTO getOwnerDTO = new GetOwnerDTO(ownerDTO, "");
        Mockito.when(ownerService.getOwner(any())).thenReturn(getOwnerDTO);
        Mockito.when(catService.createCat(any())).thenReturn(catDTO);
        Mockito.when(userService.checkUserAuthenticated(Long.valueOf(1))).thenReturn(true);

        MvcResult asyncResult = mockMvc.perform(post("/cats")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(createCatDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(catDTO)));

        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(Long.valueOf(1));
        Mockito.verify(ownerService, Mockito.times(1)).getOwner(any());
        Mockito.verify(catService, Mockito.times(1)).createCat(any());
    }

    @Test
    @WithAnonymousUser
    public void getCatHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/cats/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getCatByAdminSuccessfullyTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(get("/cats/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(catDTO)));

        Mockito.verify(userService, Mockito.times(1)).checkAdmin(any());
        Mockito.verify(catService, Mockito.times(1)).getCatById(any());
    }

    @Test
    @WithMockUser
    public void getCatByAdminHandleNotFoundTest() throws Exception {
        GetCatDTO getCatDTO = new GetCatDTO(null, "not found");

        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(get("/cats/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).checkAdmin(any());
        Mockito.verify(catService, Mockito.times(1)).getCatById(any());
    }

    @Test
    @WithMockUser
    public void getCatByUserSuccessfullyTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(get("/cats/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(catDTO)));

        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(eq(Long.valueOf(1)), any());
        Mockito.verify(catService, Mockito.times(1)).getCatById(any());
    }

    @Test
    @WithMockUser
    public void getCatByUserCatNotFoundHandleForbiddenTest() throws Exception {
        GetCatDTO getCatDTO = new GetCatDTO(null, "not found");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(get("/cats/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());

        Mockito.verify(catService, Mockito.times(1)).getCatById(any());
    }

    @Test
    @WithMockUser
    public void getCatByUserNotOwnerHandleForbiddenTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(userService.checkAdmin(any())).thenReturn(false);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(get("/cats/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());

        Mockito.verify(userService, Mockito.times(1)).checkUserAuthenticated(eq(Long.valueOf(1)), any());
        Mockito.verify(userService, Mockito.times(1)).checkAdmin(any());
        Mockito.verify(catService, Mockito.times(1)).getCatById(any());
    }

    @Test
    @WithAnonymousUser
    public void getAllCatsHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/cats")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getAllCatsWithCriteriaTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 1, "blue", List.of());

        GetAllCatsResponse getAllCatsResponse = new GetAllCatsResponse(List.of(cat1DTO, cat2DTO));
        Mockito.when(catService.getAllCats(any())).thenReturn(getAllCatsResponse);
        Mockito.when(userService.getId(any())).thenReturn(1L);

        MvcResult asyncResult = mockMvc.perform(get("/cats")
                        .param("page", "0")
                        .param("size", "2")
                        .param("color", "GREY")
                )
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(getAllCatsResponse.cats)));

        Mockito.verify(catService, Mockito.times(1)).getAllCats(any());
    }

    @Test
    @WithAnonymousUser
    public void changeOwnerHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(patch("/cats/change/owner")
                        .param("catId", "1")
                        .param("newOwnerId", "101").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void changeOwnerHandleForbiddenTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(userService.checkAdmin(any())).thenReturn(false);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(patch("/cats/change/owner")
                        .param("catId", "1")
                        .param("newOwnerId", "101").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void changeOwnerHandleCatNotFoundTest() throws Exception {
        GetCatDTO getCatDTO = new GetCatDTO(null, "not found");

        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(patch("/cats/change/owner")
                        .param("catId", "1")
                        .param("newOwnerId", "101").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void changeOwnerByAdminSuccessfullyTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");
        ChangeOwnerResponse changeOwnerResponse = new ChangeOwnerResponse("");

        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.changeOwner(any())).thenReturn(changeOwnerResponse);

        MvcResult asyncResult = mockMvc.perform(patch("/cats/change/owner")
                        .param("catId", "1")
                        .param("newOwnerId", "101").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void changeOwnerSuccessfullyTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");
        ChangeOwnerResponse changeOwnerResponse = new ChangeOwnerResponse("");

        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.changeOwner(any())).thenReturn(changeOwnerResponse);

        MvcResult asyncResult = mockMvc.perform(patch("/cats/change/owner")
                        .param("catId", "1")
                        .param("newOwnerId", "101").with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void deleteCatHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(delete("/cats/1").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void deleteCatByAdminSuccessfullyTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(delete("/cats/1")
                .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void deleteCatByUserSuccessfullyTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(true);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(delete("/cats/1")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void deleteCatByUserHandleForbiddenTest() throws Exception {
        CatDTO catDTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        GetCatDTO getCatDTO = new GetCatDTO(catDTO, "");

        Mockito.when(userService.checkAdmin(any())).thenReturn(false);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);

        MvcResult asyncResult = mockMvc.perform(delete("/cats/1")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void deleteAllCatsHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(delete("/cats").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void deleteAllByUserHandleForbiddenTest() throws Exception {
        Mockito.when(userService.checkAdmin()).thenReturn(false);
        mockMvc.perform(delete("/cats").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void deleteAllByAdminSuccessfullyTest() throws Exception {
        Mockito.when(userService.checkAdmin()).thenReturn(true);
        mockMvc.perform(delete("/cats").with(csrf())).andExpect(status().isNoContent());
        Mockito.verify(catService, Mockito.times(1)).deleteAllCats();
    }

    @Test
    @WithAnonymousUser
    public void makeFriendsHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/cats/friends/make")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void makeFriendsByNotOwnersHandleForbiddenTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 1, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(userService.checkAdmin(any())).thenReturn(false);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/make")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void makeFriendsByOwnersSuccessfullyTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 2, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");
        CatsFriendsResponse catsFriendsResponse = new CatsFriendsResponse("");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(2)), any())).thenReturn(true);
        Mockito.when(userService.checkAdmin(any())).thenReturn(false);
        Mockito.when(catService.makeFriends(any(), eq(true))).thenReturn(catsFriendsResponse);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/make")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());
        Mockito.verify(catService, Mockito.times(1)).makeFriends(any(), eq(true));
    }

    @Test
    @WithMockUser
    public void makeFriendsByAdminSuccessfullyTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 2, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");
        CatsFriendsResponse catsFriendsResponse = new CatsFriendsResponse("");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.makeFriends(any(), eq(true))).thenReturn(catsFriendsResponse);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/make")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());
        Mockito.verify(catService, Mockito.times(1)).makeFriends(any(), eq(true));
    }

    @Test
    @WithMockUser
    public void makeFriendsHandleAlreadyFriendsTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 2, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");
        CatsFriendsResponse catsFriendsResponse = new CatsFriendsResponse("already friends");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.makeFriends(any(), eq(true))).thenReturn(catsFriendsResponse);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/make")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isBadRequest());

        Mockito.verify(catService, Mockito.times(1)).makeFriends(any(), eq(true));
    }

    @Test
    @WithAnonymousUser
    public void unmakeFriendsHandleUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/cats/friends/unmake")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void unmakeFriendsByNotOwnersHandleForbiddenTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 1, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(userService.checkAdmin(any())).thenReturn(false);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/unmake")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void unmakeFriendsByOwnersSuccessfullyTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 2, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");
        CatsFriendsResponse catsFriendsResponse = new CatsFriendsResponse("");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(1)), any())).thenReturn(false);
        Mockito.when(userService.checkUserAuthenticated(eq(Long.valueOf(2)), any())).thenReturn(true);
        Mockito.when(userService.checkAdmin(any())).thenReturn(false);
        Mockito.when(catService.makeFriends(any(), eq(false))).thenReturn(catsFriendsResponse);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/unmake")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());
        Mockito.verify(catService, Mockito.times(1)).makeFriends(any(), eq(false));
    }

    @Test
    @WithMockUser
    public void unmakeFriendsByAdminSuccessfullyTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 2, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");
        CatsFriendsResponse catsFriendsResponse = new CatsFriendsResponse("");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.makeFriends(any(), eq(false))).thenReturn(catsFriendsResponse);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/unmake")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());
        Mockito.verify(catService, Mockito.times(1)).makeFriends(any(), eq(false));
    }

    @Test
    @WithMockUser
    public void unmakeFriendsHandleAlreadyNotFriendsTest() throws Exception {
        CatDTO cat1DTO = new CatDTO(1, "cat", LocalDate.parse("2015-10-15"), "Persian", CatColors.GREY, 1, "blue", List.of());
        CatDTO cat2DTO = new CatDTO(2, "cat2", LocalDate.parse("2020-08-13"), "Persian", CatColors.GREY, 2, "blue", List.of());

        GetCatDTO getCatDTO = new GetCatDTO(cat1DTO, "");
        GetCatDTO getCatDTO2 = new GetCatDTO(cat2DTO, "");
        CatsFriendsResponse catsFriendsResponse = new CatsFriendsResponse("already not friends");

        Mockito.when(catService.getCatById(Long.valueOf(1))).thenReturn(getCatDTO);
        Mockito.when(catService.getCatById(Long.valueOf(101))).thenReturn(getCatDTO2);
        Mockito.when(userService.checkAdmin(any())).thenReturn(true);
        Mockito.when(catService.makeFriends(any(), eq(false))).thenReturn(catsFriendsResponse);

        MvcResult asyncResult = mockMvc.perform(post("/cats/friends/unmake")
                        .param("firstCatId", "1")
                        .param("secondCatId", "101")
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isBadRequest());

        Mockito.verify(catService, Mockito.times(1)).makeFriends(any(), eq(false));
    }
}
