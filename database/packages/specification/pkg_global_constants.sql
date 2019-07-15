create or replace PACKAGE PKG_GLOBAL_CONSTANTS AS 

  g_success         CONSTANT VARCHAR2(7) := 'SUCCESS';
  g_fail            CONSTANT VARCHAR2(4) := 'FAIL';
  g_tag_item        CONSTANT VARCHAR2(4) := 'item';
  g_tag_category    CONSTANT VARCHAR2(8) := 'category';
  g_tag_brand       CONSTANT VARCHAR2(5) := 'brand';
  g_tag_item_info   CONSTANT VARCHAR2(9) := 'info';
  g_tag_list_item   CONSTANT VARCHAR2(9) := 'list_item';
  g_tag_list        CONSTANT VARCHAR2(4) := 'list';
  g_tag_shop        CONSTANT VARCHAR2(4) := 'shop';

END PKG_GLOBAL_CONSTANTS;
