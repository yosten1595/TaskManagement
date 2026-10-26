-- 初期スキーマ。JPA エンティティ（com.taskboard.api.entity）と一致させる。
-- ddl-auto=update で作られていた既存の DB は、Flyway の baseline（バージョン 1）として引き継ぐので、この V1 は実行されない。

CREATE TABLE boards (
    id         uuid                     NOT NULL,
    title      varchar(255)             NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT boards_pkey PRIMARY KEY (id)
);

CREATE TABLE lists (
    id       uuid         NOT NULL,
    board_id uuid         NOT NULL,
    title    varchar(255) NOT NULL,
    position integer      NOT NULL,
    CONSTRAINT lists_pkey PRIMARY KEY (id),
    CONSTRAINT lists_board_fk FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE
);

CREATE TABLE cards (
    id          uuid                     NOT NULL,
    list_id     uuid                     NOT NULL,
    title       varchar(255)             NOT NULL,
    description text,
    due_date    date,
    priority    varchar(255),
    warn_days   integer                  NOT NULL,
    position    integer                  NOT NULL,
    created_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT cards_pkey PRIMARY KEY (id),
    CONSTRAINT cards_list_fk FOREIGN KEY (list_id) REFERENCES lists (id) ON DELETE CASCADE,
    CONSTRAINT cards_priority_check CHECK (priority IN ('HIGH', 'MID', 'LOW'))
);

CREATE TABLE check_items (
    id       uuid         NOT NULL,
    card_id  uuid         NOT NULL,
    text     varchar(255) NOT NULL,
    done     boolean      NOT NULL,
    position integer      NOT NULL,
    CONSTRAINT check_items_pkey PRIMARY KEY (id),
    CONSTRAINT check_items_card_fk FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE
);

CREATE TABLE labels (
    id    uuid         NOT NULL,
    name  varchar(255) NOT NULL,
    color varchar(7)   NOT NULL,
    CONSTRAINT labels_pkey PRIMARY KEY (id)
);

CREATE TABLE card_labels (
    card_id  uuid NOT NULL,
    label_id uuid NOT NULL,
    CONSTRAINT card_labels_pkey PRIMARY KEY (card_id, label_id),
    CONSTRAINT card_labels_card_fk FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE,
    CONSTRAINT card_labels_label_fk FOREIGN KEY (label_id) REFERENCES labels (id) ON DELETE CASCADE
);

CREATE TABLE templates (
    id          uuid                     NOT NULL,
    title       varchar(255)             NOT NULL,
    description text,
    priority    varchar(255),
    warn_days   integer                  NOT NULL,
    created_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT templates_pkey PRIMARY KEY (id),
    CONSTRAINT templates_priority_check CHECK (priority IN ('HIGH', 'MID', 'LOW'))
);

CREATE TABLE template_check_items (
    id          uuid         NOT NULL,
    template_id uuid         NOT NULL,
    text        varchar(255) NOT NULL,
    position    integer      NOT NULL,
    CONSTRAINT template_check_items_pkey PRIMARY KEY (id),
    CONSTRAINT template_check_items_template_fk FOREIGN KEY (template_id) REFERENCES templates (id) ON DELETE CASCADE
);

CREATE TABLE template_labels (
    template_id uuid NOT NULL,
    label_id    uuid NOT NULL,
    CONSTRAINT template_labels_pkey PRIMARY KEY (label_id, template_id),
    CONSTRAINT template_labels_template_fk FOREIGN KEY (template_id) REFERENCES templates (id) ON DELETE CASCADE,
    CONSTRAINT template_labels_label_fk FOREIGN KEY (label_id) REFERENCES labels (id) ON DELETE CASCADE
);
