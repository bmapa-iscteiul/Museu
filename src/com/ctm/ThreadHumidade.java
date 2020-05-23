package com.ctm;

public class ThreadHumidade extends MedicaoThread {

	public ThreadHumidade(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("hum");
	}
	
	
	public void run() {
		
	}
	
}
