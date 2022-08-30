/**
 *
 */
package de.hybris.platform.b2bacceleratoraddon.forms;

/**
 * @author radami
 *
 */
public class EntryPropertiesPlus
{
	private String purchaseOrderNumber;
	private String cgi;
	private String cup;
	private String dataOrdine;
	private String entryCode;

	/**
	 * @return the purchaseOrderNumber
	 */
	public String getPurchaseOrderNumber()
	{
		return purchaseOrderNumber;
	}

	/**
	 * @param purchaseOrderNumber
	 *           the purchaseOrderNumber to set
	 */
	public void setPurchaseOrderNumber(final String purchaseOrderNumber)
	{
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	/**
	 * @return the cgi
	 */
	public String getCgi()
	{
		return cgi;
	}

	/**
	 * @param cgi
	 *           the cgi to set
	 */
	public void setCgi(final String cgi)
	{
		this.cgi = cgi;
	}

	/**
	 * @return the cup
	 */
	public String getCup()
	{
		return cup;
	}

	/**
	 * @param cup
	 *           the cup to set
	 */
	public void setCup(final String cup)
	{
		this.cup = cup;
	}

	/**
	 * @return the dataOrdine
	 */
	public String getDataOrdine()
	{
		return dataOrdine;
	}

	/**
	 * @param dataOrdine
	 *           the dataOrdine to set
	 */
	public void setDataOrdine(final String dataOrdine)
	{
		this.dataOrdine = dataOrdine;
	}

	/**
	 * @return the entryCode
	 */
	public String getEntryCode()
	{
		return entryCode;
	}

	/**
	 * @param entryCode the entryCode to set
	 */
	public void setEntryCode(String entryCode)
	{
		this.entryCode = entryCode;
	}
}
