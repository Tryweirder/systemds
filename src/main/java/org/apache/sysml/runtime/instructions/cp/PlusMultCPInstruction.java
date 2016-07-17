package org.apache.sysml.runtime.instructions.cp;

import org.apache.sysml.runtime.DMLRuntimeException;
import org.apache.sysml.runtime.controlprogram.context.ExecutionContext;
import org.apache.sysml.runtime.functionobjects.MinusMultiply;
import org.apache.sysml.runtime.functionobjects.PlusMultiply;
import org.apache.sysml.runtime.functionobjects.ValueFunctionWithConstant;
import org.apache.sysml.runtime.instructions.InstructionUtils;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;
import org.apache.sysml.runtime.matrix.operators.BinaryOperator;

public class PlusMultCPInstruction extends ArithmeticBinaryCPInstruction {
	public PlusMultCPInstruction(BinaryOperator op, CPOperand in1, CPOperand in2, 
			CPOperand in3, CPOperand out, String opcode, String str) 
	{
		super(op, in1, in2, out, opcode, str);
		input3=in3;
	}
	public static PlusMultCPInstruction parseInstruction(String str)
	{
		String[] parts = InstructionUtils.getInstructionPartsWithValueType(str);
		String opcode=parts[0];
		CPOperand operand1 = new CPOperand(parts[1]);
		CPOperand operand2 = new CPOperand(parts[3]); //put the second matrix (parts[3]) in Operand2 to make using Binary matrix operations easier
		CPOperand operand3 = new CPOperand(parts[2]); 
		CPOperand outOperand = new CPOperand(parts[4]);
		BinaryOperator bOperator = null;
		if(opcode.equals("+*"))
			bOperator = new BinaryOperator(new PlusMultiply());
		else if (opcode.equals("-*"))
			bOperator = new BinaryOperator(new MinusMultiply());
		return new PlusMultCPInstruction(bOperator,operand1, operand2, operand3, outOperand, opcode,str);
		
	}
	@Override
	public void processInstruction( ExecutionContext ec )
		throws DMLRuntimeException
	{		
		String output_name = output.getName();

		//get all the inputs
		MatrixBlock matrix1 = ec.getMatrixInput(input1.getName());
		MatrixBlock matrix2 = ec.getMatrixInput(input2.getName());
		ScalarObject lambda = ec.getScalarInput(input3.getName(), input3.getValueType(), input3.isLiteral()); 
		
		//execution
		((ValueFunctionWithConstant) ((BinaryOperator)_optr).fn).setConstant(lambda.getDoubleValue());
		MatrixBlock out = (MatrixBlock) matrix1.binaryOperations((BinaryOperator) _optr, matrix2, new MatrixBlock());
		
		//release the matrices
		ec.releaseMatrixInput(input1.getName());
		ec.releaseMatrixInput(input2.getName());
		
		ec.setMatrixOutput(output_name, out);
	}
}
