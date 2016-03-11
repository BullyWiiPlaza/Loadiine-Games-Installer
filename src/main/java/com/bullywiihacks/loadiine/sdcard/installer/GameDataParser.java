package com.bullywiihacks.loadiine.sdcard.installer;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class GameDataParser
{
	private Document document;

	public GameDataParser(String metaXMLFilePath) throws ParserConfigurationException, IOException, SAXException
	{
		File metaXMLFile = new File(metaXMLFilePath);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		document = documentBuilder.parse(metaXMLFile);
	}

	public String getGameName()
	{
		return getText("longname_en");
	}

	public String getGameId() throws IOException, SAXException, ParserConfigurationException
	{
		String productCode = getProductCode();
		String companyCode = getCompanyCode();

		return productCode + companyCode;
	}

	private String getProductCode()
	{
		return getText("product_code", 6);
	}

	private String getCompanyCode()
	{
		return getText("company_code", 2);
	}

	private String getText(String tagName)
	{
		return getText(tagName, 0);
	}

	private String getText(String tagName, int beginIndex)
	{
		return document.getElementsByTagName(tagName).item(0).getTextContent().substring(beginIndex);
	}
}