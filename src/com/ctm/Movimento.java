package com.ctm;

public class Movimento extends MedicaoThread {

	public Movimento(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("mov");
	}
	
	public void run() {
		
	}

}
