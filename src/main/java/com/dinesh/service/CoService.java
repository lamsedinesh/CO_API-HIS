package com.dinesh.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialException;

import com.dinesh.entity.CoTriggerEntity;

import com.dinesh.entity.EligibilityDtls;

public interface CoService {
	public String processCoTriggers() throws SerialException, SQLException, IOException;

	public void generatePdf(EligibilityDtls dtls);

	public boolean updateCoTriggerRecord(CoTriggerEntity triggerEntity, FileInputStream fis);
}
