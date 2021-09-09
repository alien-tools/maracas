package mainclient.annotationDeprecatedAdded;

import main.annotationDeprecatedAdded.AnnDeprAddedNonEmptyClass;

public class AnnotationDeprecatedAddedExt extends AnnDeprAddedNonEmptyClass {

	public void deprecatedNoSuperKey() {
		int f = transField;
		transMethod();
	}
	
	public void deprecatedSuperKey() {
		int f = super.transField;
		super.transMethod();
	}
	
}
