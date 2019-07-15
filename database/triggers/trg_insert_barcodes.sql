create or replace TRIGGER trg_insert_barcodes
AFTER INSERT ON items_xml
FOR EACH ROW
WHEN (NEW.barcode IS NOT NULL)
  BEGIN
    -- Need to ensure that record for the barcode does not already exist.
    INSERT INTO barcodes (barcode,
                          item_name)
      SELECT :NEW.barcode,
             :NEW.item_name
      FROM dual
      WHERE NOT EXISTS (SELECT 1
                        FROM barcodes
                        WHERE barcode = :NEW.barcode);
  END;
