CREATE TABLE SHOPLIST.BRANDS_XML (
  BRAND_ID NUMBER(8,0), 
	BRAND_NAME VARCHAR2(40 BYTE), 
	USER_ID NUMBER(8,0), 
	CONSTRAINT PK_BRAND_XML PRIMARY KEY (BRAND_ID, USER_ID)
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
    PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE SYSTEM ENABLE, 
	CONSTRAINT FK_BRANDS_USER_ID FOREIGN KEY (USER_ID)
	  REFERENCES SHOPLIST.USERS_XML (USER_ID) ENABLE
)
SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
    PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;

COMMENT ON TABLE SHOPLIST.BRANDS_XML  IS 'xml_tag:brand';
