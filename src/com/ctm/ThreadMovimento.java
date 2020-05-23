package com.ctm;

public class ThreadMovimento extends MedicaoThread {

	public ThreadMovimento(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("mov");
	}
	
	public void run() {
		
	}

}
