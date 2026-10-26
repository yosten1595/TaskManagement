package com.taskboard.api.util;

public final class Texts {

	private Texts() {
	}

	// 空文字・空白だけの入力は「未設定」として null にする
	public static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	// LIKE の特殊文字（バックスラッシュ、%、_）を通常の文字として扱えるようにエスケープする。escape 文字はバックスラッシュ
	public static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
