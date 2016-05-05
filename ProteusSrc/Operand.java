//Excluding package for now until can resolve with makefile (having packagename leads to "class not found" error. Maybe because source not in path specified by package name?)
//package com.columbia.edu.comse6156.ProteusObfuscatorVersionTwo;
public class Operand 
{
		private String type;
		private String name;
		private boolean isSecret;

		// -- allows us to create a Person via the constructor
		public Operand(String thetype, String thename) {
		this.type = thetype;
		this.name = thename;
		this.isSecret = false;
		}

		
		public Operand() {
		this.type = "";
		this.name = "";
		this.isSecret = false;
		}

		// -- accessors
		public String getType() { return type; }

		public String getName() { return name; }

		public boolean isThisSecret() { return isSecret; }

		// -- mutators
		public void setType(String thetype) { this.type = thetype; }

		public void setName(String thename) {
		this.name = thename;
		}

		public void setToSecret(boolean val) {
		this.isSecret = val;
		}
}
