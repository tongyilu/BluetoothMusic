package com.spreadwin.btc.utils;


public class MobileLocation {

	private String resultcode;

	private String reason;

	private Result result;

	private int error_code;

	public void setResultcode(String resultcode) {
		this.resultcode = resultcode;
	}

	public String getResultcode() {
		return this.resultcode;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return this.reason;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return this.result;
	}

	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	public int getError_code() {
		return this.error_code;
	}

	public class Result {
		private String province;

		private String city;

		private String areacode;

		private String zip;

		private String company;

		private String card;

		public void setProvince(String province) {
			this.province = province;
		}

		public String getProvince() {
			return this.province;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getCity() {
			return this.city;
		}

		public void setAreacode(String areacode) {
			this.areacode = areacode;
		}

		public String getAreacode() {
			return this.areacode;
		}

		public void setZip(String zip) {
			this.zip = zip;
		}

		public String getZip() {
			return this.zip;
		}

		public void setCompany(String company) {
			this.company = company;
		}

		public String getCompany() {
			return this.company;
		}

		public void setCard(String card) {
			this.card = card;
		}

		public String getCard() {
			return this.card;
		}
	}
	
	
}
