CREATE TABLE CATS_FRIENDSHIP (
                                 first_cat_id BIGINT,
                                 second_cat_id BIGINT,
                                 PRIMARY KEY (first_cat_id, second_cat_id),
                                 CONSTRAINT fk_first_cat FOREIGN KEY (first_cat_id) REFERENCES CATS(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_second_cat FOREIGN KEY (second_cat_id) REFERENCES CATS(id) ON DELETE CASCADE
);
