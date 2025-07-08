alter table CATS add column eyes_color VARCHAR(255),
alter column eyes_color SET DEFAULT 'green';

alter table CATS alter column eyes_color SET NOT NULL;
