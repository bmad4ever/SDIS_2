
public class RefBoolean {
		
	//possibly used for debbuging (might be set to null if not needed)
	private String description;
	public String getDescription(){return description;}
	
	private volatile Boolean value = null;
	public void setValue(boolean value){this.value = value;}
	public Boolean getValue(){return this.value;}
	
	RefBoolean(String description) {
		this.description = description;
	} 
	
}
