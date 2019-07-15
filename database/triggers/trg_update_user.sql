create or replace TRIGGER trg_update_user
BEFORE UPDATE ON users_xml
FOR EACH ROW
  WHEN ((OLD.backup_file IS NOT NULL AND NEW.backup_file != OLD.backup_file) OR
        (NEW.user_name != OLD.user_name))
    BEGIN
      IF :NEW.backup_file != :OLD.backup_file THEN
        :NEW.previous_backup_file := :OLD.backup_file;
        :NEW.previous_backup_date := :OLD.backup_date;
        
      ELSIF :NEW.user_name != :OLD.user_name THEN
        :NEW.previous_user_name := :OLD.user_name;
        :NEW.user_name_change_date := SYSDATE;
        
      END IF;
    END;
