package com.taskboard.api.controller;

import com.taskboard.api.dto.BackupDto;
import com.taskboard.api.service.BackupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BackupController {

	private final BackupService backupService;

	// 全ボード・テンプレートを、インポートできる形式で返す
	@GetMapping("/export")
	public BackupDto export() {
		return backupService.export();
	}

	// ファイルの内容を新しいボード・テンプレートとして追加する（既存のデータは消さない）
	@PostMapping("/import")
	@ResponseStatus(HttpStatus.CREATED)
	public BackupService.ImportResult importBackup(@Valid @RequestBody BackupDto backup) {
		return backupService.importBackup(backup);
	}
}
