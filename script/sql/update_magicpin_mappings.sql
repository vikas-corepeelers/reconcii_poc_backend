UPDATE magicpin
JOIN magicpin_mappings ON magicpin.mid = magicpin_mappings.mid
SET
    magicpin.mcd_store_code = magicpin_mappings.store_code,
    magicpin.actual_packaging_charge = CASE
                                        WHEN magicpin.item_amount != 0 THEN magicpin_mappings.packaging_charge
                                        ELSE 0
                                    END
WHERE
    magicpin.mcd_store_code IS NULL;
