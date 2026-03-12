CALL ADD_COLUMN('product', 'additional', 'BIT(1) DEFAULT FALSE');

UPDATE product SET additional = TRUE
WHERE action_code IN ("SZ-20",
                      "SCHNUPPER2007",
                      "VERL-MANUELL-NORM",
                      "VERL-MANUELL-ERM",
                      "EIN-EURO-AKT",
                      "EIN-EURO-0906");
