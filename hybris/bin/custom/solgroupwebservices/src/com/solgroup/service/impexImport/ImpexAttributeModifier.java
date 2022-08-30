package com.solgroup.service.impexImport;

public class ImpexAttributeModifier {
	private String modifier;
	private TypeBrackets brackets;

	public enum TypeBrackets {
		ROUNDS, SQUARE
	};

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public ImpexAttributeModifier(String modifier, TypeBrackets brackets) {
		super();
		this.modifier = modifier;
		this.brackets = brackets;
	}

	public String toHeader() {
		if (brackets == TypeBrackets.ROUNDS)
			return "(" + modifier + ")";
		else
			return "[" + modifier + "]";
	}
}
