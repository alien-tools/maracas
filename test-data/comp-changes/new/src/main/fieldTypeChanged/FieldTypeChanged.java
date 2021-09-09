package main.fieldTypeChanged;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FieldTypeChanged extends FieldTypeChangedSuper {
	public int unchangedPrimitive;
	public A unchangedReference;
	
	public A changedPrimitive;
	public C changedReference;
	
	public short widenPrimitive;
	public A widenReference;
	
	public byte narrowPrimitive;
	public B narrowReference;
	
	public Integer boxing;
	public int unboxing;
	public Integer changedBoxing;
	
	public List<B> narrowReferenceList;
	public List<A> widenReferenceList;
	public List<C> changedReferenceList;
	
	public ArrayList<A> narrowListReference;
	public Collection<A> widenListReference;
	
	public short[] narrowPrimitiveArray;
	public long[] widenPrimitiveArray;
	
	public Integer[] boxPrimitiveArray;
	public int[] unboxPrimitiveArray;
	
	public B[] narrowReferenceArray;
	public A[] widenReferenceArray;
	public C[] changedReferenceArray;
	
	public A exceptionTypeChanged;
}
