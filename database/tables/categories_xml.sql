CREATE TABLE SHOPLIST.CATEGORIES_XML (
  CAT_ID NUMBER(8,0), 
	CATEGORY_NAME VARCHAR2(40 BYTE), 
	USER_ID NUMBER(8,0), 
	CONSTRAINT PK_CAT_XML PRIMARY KEY (CAT_ID, USER_ID)
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
    PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE, 
	CONSTRAINT FK_CATEGORIES_USER_ID FOREIGN KEY (USER_ID)
	  REFERENCES SHOPLIST.USERS_XML (USER_ID) ENABLE
)
SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
    PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE SYSTEM ;

COMMENT ON TABLE SHOPLIST.CATEGORIES_XML  IS 'xml_tag:category';
