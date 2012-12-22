package org.osk.models;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.ports.FluidPort;

public interface Tank {

	void init(String name);

	ImmutablePair<FluidPort, FluidPort> calculateOutletsMassFlow(
			FluidPort inputPortFuelPressureGas,
			FluidPort inputPortOxidizerPressureGas);

	ImmutablePair<FluidPort, FluidPort> propagate(final double time,
			final double tStepSize,
			final FluidPort inputPortOxidizerPressureGas,
			final FluidPort inputPortFuelPressureGas);

	@ManagedAttribute
	String getFuel();

	void setFuel(String fuel);

	@ManagedAttribute
	String getOxidizer();

	void setOxidizer(String oxidizer);

	@ManagedAttribute
	String getFuPressGas();

	void setFuPressGas(String fuPressGas);

	@ManagedAttribute
	String getOxPressGas();

	void setOxPressGas(String oxPressGas);

	@ManagedAttribute
	double getMfBoundFuelPress();

	void setMfBoundFuelPress(double mfBoundFuelPress);

	@ManagedAttribute
	double getMfBoundOxPress();

	void setMfBoundOxPress(double mfBoundOxPress);

	@ManagedAttribute
	double getpBoundFuelPress();

	void setpBoundFuelPress(double pBoundFuelPress);

	@ManagedAttribute
	double getpBoundOxPress();

	void setpBoundOxPress(double pBoundOxPress);

	@ManagedAttribute
	double getMfBoundFuel();

	void setMfBoundFuel(double mfBoundFuel);

	@ManagedAttribute
	double getMfBoundOx();

	void setMfBoundOx(double mfBoundOx);

	@ManagedAttribute
	double getPinFPG();

	void setPinFPG(double pinFPG);

	@ManagedAttribute
	double getTinFPG();

	void setTinFPG(double tinFPG);

	@ManagedAttribute
	double getMfinFPG();

	void setMfinFPG(double mfinFPG);

	@ManagedAttribute
	double getPinOPG();

	void setPinOPG(double pinOPG);

	@ManagedAttribute
	double getTinOPG();

	void setTinOPG(double tinOPG);

	@ManagedAttribute
	double getMfinOPG();

	void setMfinOPG(double mfinOPG);

	@ManagedAttribute
	double getPoutFuel();

	void setPoutFuel(double poutFuel);

	@ManagedAttribute
	double getToutFuel();

	void setToutFuel(double toutFuel);

	@ManagedAttribute
	double getMfoutFuel();

	void setMfoutFuel(double mfoutFuel);

	@ManagedAttribute
	double getPoutOxidizer();

	void setPoutOxidizer(double poutOxidizer);

	@ManagedAttribute
	double getToutOxidizer();

	void setToutOxidizer(double toutOxidizer);

	@ManagedAttribute
	double getMfoutOxidizer();

	void setMfoutOxidizer(double mfoutOxidizer);

	@ManagedAttribute
	double getVTBR();

	void setVTBR(double vTBR);

	@ManagedAttribute
	double getVTOX();

	void setVTOX(double vTOX);

	@ManagedAttribute
	double getFAWO();

	void setFAWO(double fAWO);

	@ManagedAttribute
	double getFTWO();

	void setFTWO(double fTWO);

	@ManagedAttribute
	double getFAWB();

	void setFAWB(double fAWB);

	@ManagedAttribute
	double getFTWB();

	void setFTWB(double fTWB);

	@ManagedAttribute
	double getHGOX();

	void setHGOX(double hGOX);

	@ManagedAttribute
	double getHGBR();

	void setHGBR(double hGBR);

	@ManagedAttribute
	double getCHARMO();

	void setCHARMO(double cHARMO);

	@ManagedAttribute
	double getCHARMB();

	void setCHARMB(double cHARMB);

	@ManagedAttribute
	double getFMAWO();

	void setFMAWO(double fMAWO);

	@ManagedAttribute
	double getFMTW();

	void setFMTW(double fMTW);

	@ManagedAttribute
	double getFMAWB();

	void setFMAWB(double fMAWB);

	@ManagedAttribute
	double getSPWKO();

	void setSPWKO(double sPWKO);

	@ManagedAttribute
	double getSPWKB();

	void setSPWKB(double sPWKB);

	@ManagedAttribute
	double getPTO();

	void setPTO(double pTO);

	@ManagedAttribute
	double getPENDOX();

	void setPENDOX(double pENDOX);

	@ManagedAttribute
	double getPTB();

	void setPTB(double pTB);

	@ManagedAttribute
	double getPENDBR();

	void setPENDBR(double pENDBR);

	@ManagedAttribute
	double getVANFOX();

	void setVANFOX(double vANFOX);

	@ManagedAttribute
	double getTANFOX();

	void setTANFOX(double tANFOX);

	@ManagedAttribute
	double getVANFBR();

	void setVANFBR(double vANFBR);

	@ManagedAttribute
	double getTANFBR();

	void setTANFBR(double tANFBR);

	@ManagedAttribute
	double getPVO();

	void setPVO(double pVO);

	@ManagedAttribute
	double getTHEINO();

	void setTHEINO(double tHEINO);

	@ManagedAttribute
	double getTHEINB();

	void setTHEINB(double tHEINB);

	@ManagedAttribute
	double getMAWGO();

	void setMAWGO(double mAWGO);

	@ManagedAttribute
	double getMTWGO();

	void setMTWGO(double mTWGO);

	@ManagedAttribute
	double getMAWGB();

	void setMAWGB(double mAWGB);

	@ManagedAttribute
	double getMTWGB();

	void setMTWGB(double mTWGB);

	@ManagedAttribute
	double getMPHEOX();

	void setMPHEOX(double mPHEOX);

	@ManagedAttribute
	double getMPHEBR();

	void setMPHEBR(double mPHEBR);

	@ManagedAttribute
	double getMAWGOA();

	void setMAWGOA(double mAWGOA);

	@ManagedAttribute
	double getMTWGOA();

	void setMTWGOA(double mTWGOA);

	@ManagedAttribute
	double getMAWGBA();

	void setMAWGBA(double mAWGBA);

	@ManagedAttribute
	double getMTWGBA();

	void setMTWGBA(double mTWGBA);

	@ManagedAttribute
	double getMLOX();

	void setMLOX(double mLOX);

	@ManagedAttribute
	double getMLBR();

	void setMLBR(double mLBR);

	@ManagedAttribute
	double getMPKTLO();

	void setMPKTLO(double mPKTLO);

	@ManagedAttribute
	double getMPKTLB();

	void setMPKTLB(double mPKTLB);

	@ManagedAttribute
	double getMDO();

	void setMDO(double mDO);

	@ManagedAttribute
	double getMHEOXA();

	void setMHEOXA(double mHEOXA);

	@ManagedAttribute
	double getMHEBRA();

	void setMHEBRA(double mHEBRA);

	@ManagedAttribute
	double[] getFuLevel();

	void setFuLevel(double[] fuLevel);

	@ManagedAttribute
	double[] getFuCOutWSfc();

	void setFuCOutWSfc(double[] fuCOutWSfc);

	@ManagedAttribute
	double[] getFuCSepWSfc();

	void setFuCSepWSfc(double[] fuCSepWSfc);

	@ManagedAttribute
	double[] getFuSfc();

	void setFuSfc(double[] fuSfc);

	@ManagedAttribute
	double[] getFuCOutWSfc2();

	void setFuCOutWSfc2(double[] fuCOutWSfc2);

	@ManagedAttribute
	double[] getFuCSepWSfc2();

	void setFuCSepWSfc2(double[] fuCSepWSfc2);

	@ManagedAttribute
	double[] getFuSfc2();

	void setFuSfc2(double[] fuSfc2);

	@ManagedAttribute
	double[] getOxLevel();

	void setOxLevel(double[] oxLevel);

	@ManagedAttribute
	double[] getOxCOutWSfc();

	void setOxCOutWSfc(double[] oxCOutWSfc);

	@ManagedAttribute
	double[] getOxCSepWSfc();

	void setOxCSepWSfc(double[] oxCSepWSfc);

	@ManagedAttribute
	double[] getOxSfc();

	void setOxSfc(double[] oxSfc);

	@ManagedAttribute
	double[] getOxCOutWSfc2();

	void setOxCOutWSfc2(double[] oxCOutWSfc2);

	@ManagedAttribute
	double[] getOxCSepWSfc2();

	void setOxCSepWSfc2(double[] oxCSepWSfc2);

	@ManagedAttribute
	double[] getOxSfc2();

	void setOxSfc2(double[] oxSfc2);

	@ManagedAttribute
	int getIFMAX();

	void setIFMAX(int iFMAX);

	@ManagedAttribute
	int getIFANZ();

	void setIFANZ(int iFANZ);

	@ManagedAttribute
	int getIFEHL();

	void setIFEHL(int iFEHL);

	@ManagedAttribute
	int getBDFLAG();

	void setBDFLAG(int bDFLAG);

	@ManagedAttribute
	double getPoxt();

	void setPoxt(double poxt);

	@ManagedAttribute
	double gettGOxT();

	void settGOxT(double tGOxT);

	@ManagedAttribute
	double gettLOxT();

	void settLOxT(double tLOxT);

	@ManagedAttribute
	double getPFuT();

	void setPFuT(double pFuT);

	@ManagedAttribute
	double gettGFuT();

	void settGFuT(double tGFuT);

	@ManagedAttribute
	double gettLFuT();

	void settLFuT(double tLFuT);

}