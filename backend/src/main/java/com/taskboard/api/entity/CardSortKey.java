package com.taskboard.api.entity;

// リスト内のカードを並べ替える基準
public enum CardSortKey {
	// 期限が近い順
	DUE_DATE,
	// 優先度が高い順（同順位は期限が近い順）
	PRIORITY
}
