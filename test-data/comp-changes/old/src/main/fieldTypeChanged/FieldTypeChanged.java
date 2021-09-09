package main.fieldTypeChanged;

import java.util.List;

public class FieldTypeChanged extends FieldTypeChangedSuper {
	public int unchangedPrimitive;
	public A unchangedReference;
	
	public int changedPrimitive;
	public A changedReference;

	public byte widenPrimitive;
	public B widenReference;
	
	public short narrowPrimitive;
	public A narrowReference;
	
	public int boxing;
	public Integer unboxing;
	public boolean changedBoxing;
	
	public List<A> narrowReferenceList;
	public List<B> widenReferenceList;
	public List<A> changedReferenceList;
	
	public List<A> narrowListReference;
	public List<A> widenListReference; 
	
	public int[] narrowPrimitiveArray;
	public int[] widenPrimitiveArray;
	
	public int[] boxPrimitiveArray;
	public Integer[] unboxPrimitiveArray;
	
	public A[] narrowReferenceArray;
	public B[] widenReferenceArray;
	public A[] changedReferenceArray;
	
	public Exception exceptionTypeChanged;
}
