-- テストデータ作る
-- users(仮)
INSERT INTO users (username, password_hash, updated_at)
VALUES 
('user1', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', CURRENT_TIMESTAMP), 
('user2', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', CURRENT_TIMESTAMP);

-- notification_settings(仮)
INSERT INTO notification_settings (
    user_id, 
    include_tags, 
    exclude_tags, 
    webhook_url, 
    webhook_name,
    is_active
)
VALUES 
(1, 'Java,Spring', 'AI','8c4615a4c729615eea311c01a4e45a8fc7783683b95529dab6dc8af3dafa876a9f623cdcdfaa8c4c30e189d92837f4a9ab12b1b651d4da39cf1f2a1a901777fc014d598f7c48331d81d815fb203e78659a697e9dc2c9d8c840481126514ade339e34df31034fa36e184dc6386086b02e533349d922706a73a0eed4985bd11dda70272676d8b74abe24f53cf2779cf9be67a33bf53d56658553', 'ch1', true),
(2, 'Python,Django', '機械学習', 'c6d23d3ba6d8bce14c38e08d04b9ff87d68aa4d0fd220b662e1f265776c7c848dbebad64da48db10d5131c32737e04689f770494416dcd27665d17f8c96a72da70fa170e6737aeaa88d95c808aa8aba91564201faf316845f0c443ced7736c980d53baa89e5004baaf9c4ed9', 'ch2', true);

-- notification_log(仮)
INSERT INTO notification_logs (setting_id, article_id)
VALUES (1, 'test_article_id')

