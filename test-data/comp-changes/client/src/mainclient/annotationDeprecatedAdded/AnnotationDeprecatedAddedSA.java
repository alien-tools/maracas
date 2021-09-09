package mainclient.annotationDeprecatedAdded;

import java.util.List;

import main.annotationDeprecatedAdded.AnnDeprAddedEmptyClass;
import main.annotationDeprecatedAdded.AnnDeprAddedFieldMethod;
import main.annotationDeprecatedAdded.AnnDeprAddedNonEmptyClass;
import main.annotationDeprecatedAdded.AnnDeprAddedNonEmptyClassSub;
import main.annotationDeprecatedAdded.IAnnDeprAdded;

public class AnnotationDeprecatedAddedSA {

	AnnDeprAddedEmptyClass emptyClass;
	AnnDeprAddedNonEmptyClass nonEmptyClass;
	AnnDeprAddedNonEmptyClassSub nonEmptyClassSub;
	AnnDeprAddedFieldMethod nonDepClass;
	List<IAnnDeprAdded> deprInterfaceAsTypeParam;

	public void deprecatedClassEmpty() {
		AnnDeprAddedEmptyClass a = new AnnDeprAddedEmptyClass();
	}

	public void deprecatedClassNonEmpty() {
		AnnDeprAddedNonEmptyClass a = new AnnDeprAddedNonEmptyClass();
		int f = a.transField;
		a.transMethod();
	}
	
	public void deprecatedClassNonEmptySub() {
		AnnDeprAddedNonEmptyClassSub a = new AnnDeprAddedNonEmptyClassSub();
		int f = a.transField;
		a.transMethod();
	}

	public void deprecatedField() {
		AnnDeprAddedFieldMethod a = new AnnDeprAddedFieldMethod();
		int f = a.field;
	}

	public void deprecatedMethod() {
		AnnDeprAddedFieldMethod a = new AnnDeprAddedFieldMethod();
		int m = a.method();
	}

	public void deprecatedClassAnonymous() {
		AnnDeprAddedEmptyClass a = new AnnDeprAddedEmptyClass() {};
		AnnDeprAddedNonEmptyClass b = new AnnDeprAddedNonEmptyClass() {};
	}

	public void deprecatedInterfaceAnonymous() {
		IAnnDeprAdded a = new IAnnDeprAdded() {};
	}

	public void deprecatedClassAsParameter(AnnDeprAddedEmptyClass a) {

	}

	public IAnnDeprAdded deprecatedInterfaceAsReturnType() {
		return null;
	}

	public void deprecatedInterfaceAsTypeParam() {
		List<IAnnDeprAdded> a = null;
	}
}
