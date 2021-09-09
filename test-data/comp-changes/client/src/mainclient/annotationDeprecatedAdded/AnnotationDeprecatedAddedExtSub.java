package mainclient.annotationDeprecatedAdded;

import main.annotationDeprecatedAdded.AnnDeprAddedNonEmptyClassSub;

public class AnnotationDeprecatedAddedExtSub extends AnnDeprAddedNonEmptyClassSub {

	public void deprecatedNoSuperKey() {
		int f = transField;
		transMethod();
	}
	
	public void deprecatedSuperKey() {
		int f = super.transField;
		super.transMethod();
	}
	
}
