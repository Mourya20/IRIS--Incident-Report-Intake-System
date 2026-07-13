INSERT INTO role (name)
VALUES ('REPORTER'), ('ANALYST'), ('ADMIN');

INSERT INTO severity_rule (rule_key, weight, enabled, metadata_json)
VALUES
    ('keyword:ransomware', 60, TRUE, '{"keywords":["ransomware","encrypt files"]}'),
    ('keyword:credential-theft', 45, TRUE, '{"keywords":["credential dumping","password spray"]}'),
    ('keyword:phishing', 25, TRUE, '{"keywords":["phishing","suspicious link","spoofed email"]}'),
    ('keyword:malware', 30, TRUE, '{"keywords":["malware","trojan","beacon"]}');

INSERT INTO attack_technique (technique_id, tactic, name, description)
VALUES
    ('T1566', 'Initial Access', 'Phishing', 'Adversaries send deceptive messages to gain access.'),
    ('T1003', 'Credential Access', 'OS Credential Dumping', 'Adversaries dump credentials from OS stores.'),
    ('T1059.001', 'Execution', 'PowerShell', 'Adversaries execute commands and scripts via PowerShell.'),
    ('T1486', 'Impact', 'Data Encrypted for Impact', 'Adversaries encrypt data to interrupt operations.');
