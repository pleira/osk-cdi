package org.osk;


public class ScData {

    //  Entries for Celestia interface variables.
    final  String celestiaTime;
    final  double xPosition;
    final  double yPosition;
    final  double zPosition;
    final  double wQuat;
    final  double xQuat;
    final  double yQuat;
    final  double zQuat;
    
    public static class Builder {
        String celestiaTime;
        double[] position;
        double[] quaternion;
        
    	public Builder withTime(String celestiaTime) {
    		this.celestiaTime = celestiaTime;
    		return this;
    		}
    	
    	public Builder withPosition(double[] position) {
    		this.position = position;
    		return this;
    	}
    	
    	public Builder withQuaternion(double[] quaternion) {
    		this.quaternion = quaternion;
    		return this;
    	}
    	
    	public ScData build() {
    		return new ScData(celestiaTime, position, quaternion);
    	}
    }
    
    
	public ScData(String celestiaTime, double xPosition, double yPosition,
			double zPosition, double wQuat, double xQuat, double yQuat,
			double zQuat) {
		super();
		this.celestiaTime = celestiaTime;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.zPosition = zPosition;
		this.wQuat = wQuat;
		this.xQuat = xQuat;
		this.yQuat = yQuat;
		this.zQuat = zQuat;
	}

	public ScData(String celestiaTime, double[] scPosictionECI, double[] quaternionInComponent) {
		this.celestiaTime = celestiaTime;
        // Celestia needs the position vector in [km], not in [m]. 
		this.xPosition = scPosictionECI[0]/1000.0;
		this.yPosition = scPosictionECI[1]/1000.0;
		this.zPosition = scPosictionECI[2]/1000.0;
		this.wQuat = quaternionInComponent[3];
		this.xQuat = quaternionInComponent[0];
		this.yQuat = quaternionInComponent[1];
		this.zQuat = quaternionInComponent[2];
		
	}
	
	public byte[] getCelestiaInfo() {
		StringBuilder b = new StringBuilder();
		b.append(xPosition); b.append('/');
		b.append(yPosition); b.append('/');
		b.append(zPosition); b.append('/');
		b.append(wQuat); b.append('/');
		b.append(xQuat); b.append('/');
		b.append(yQuat); b.append('/');
		b.append(zQuat); b.append('/');
		b.append(celestiaTime); b.append("\n");
				
		return b.toString().getBytes();
		
	}
}
