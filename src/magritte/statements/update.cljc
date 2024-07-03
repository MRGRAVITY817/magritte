(ns magritte.statements.update)

(defn format-update
  "Formats an update statement.

  ```
  UPDATE [ ONLY ] @targets
    [ CONTENT @value
      | MERGE @value
      | PATCH @value
      | [ SET @field = @value, ... | UNSET @field, ... ]
    ]
    [ WHERE @condition ]
    [ RETURN NONE | RETURN BEFORE | RETURN AFTER | RETURN DIFF | RETURN @statement_param, ... ]
    [ TIMEOUT @duration ]
    [ PARALLEL ]
  ;
  ```
  "
  [{:keys [update set]}])
