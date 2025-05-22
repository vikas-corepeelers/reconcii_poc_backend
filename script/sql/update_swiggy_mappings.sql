UPDATE swiggy
JOIN swiggy_mappings ON swiggy.restaurant_id = swiggy_mappings.swiggy_store_code
SET
    swiggy.mcd_store_code = swiggy_mappings.store_code,
    swiggy.actual_packaging_charge = CASE
                                        WHEN swiggy.item_total != 0 THEN swiggy_mappings.packaging_charge
                                        ELSE 0
                                    END
WHERE
    swiggy.mcd_store_code IS NULL;
