package com.dinesh.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dinesh.service.CoService;

@RestController
public class CoRestController {

	@Autowired
	private CoService coService;
	
	@GetMapping("/process")
	public ResponseEntity<String> processPendingTriggers() throws SerialException, SQLException, IOException{
		String response = coService.processCoTriggers();
		
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
}

