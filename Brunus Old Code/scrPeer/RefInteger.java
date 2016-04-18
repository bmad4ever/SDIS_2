
public class RefInteger {
	//possibly used for debbuging (might be set to null if not needed)
	private String description;
	public String getDescription(){return description;}
	
	private volatile Integer value = null;
	public void setValue(int value){this.value = value;}
	public Integer getValue(){return this.value;}
	
	RefInteger(String description) {
		this.description = description;
	} 
}
