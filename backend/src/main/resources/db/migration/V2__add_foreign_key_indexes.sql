-- PostgreSQL は外部キーの列に自動でインデックスを作らない。親から子を引く検索・カスケード削除を速くする。
-- 既存の DB（baseline）にも適用されるので、IF NOT EXISTS にしている。

CREATE INDEX IF NOT EXISTS lists_board_id_idx ON lists (board_id);
CREATE INDEX IF NOT EXISTS cards_list_id_idx ON cards (list_id);
CREATE INDEX IF NOT EXISTS check_items_card_id_idx ON check_items (card_id);
CREATE INDEX IF NOT EXISTS card_labels_label_id_idx ON card_labels (label_id);
CREATE INDEX IF NOT EXISTS template_check_items_template_id_idx ON template_check_items (template_id);
CREATE INDEX IF NOT EXISTS template_labels_template_id_idx ON template_labels (template_id);
