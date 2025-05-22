UPDATE zomato
JOIN zomato_mappings ON zomato.res_id = zomato_mappings.zomato_store_code
SET
    zomato.mcd_store_code = zomato_mappings.store_code,
    zomato.actual_packaging_charge = CASE
                                        WHEN zomato.bill_subtotal != 0 THEN zomato_mappings.packaging_charge
                                        ELSE 0
                                    END
WHERE
    zomato.mcd_store_code IS NULL;
