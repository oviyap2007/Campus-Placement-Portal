USE campus_placement;

-- Check current state
SELECT account_id, company_id, username, is_approved FROM company_account;

-- Force approve TCS and Infosys
UPDATE company_account SET is_approved = TRUE WHERE username = 'tcs_hr';
UPDATE company_account SET is_approved = TRUE WHERE username = 'infosys_hr';

-- Verify fix
SELECT account_id, company_id, username, is_approved FROM company_account;
