package soot.dotnet.instructions;

import soot.Body;
import soot.Value;
import soot.dotnet.members.method.DotnetBody;
import soot.dotnet.exceptions.NoStatementInstructionException;
import soot.dotnet.proto.ProtoIlInstructions;
import soot.jimple.FloatConstant;

public class CilLdcR4Instruction extends AbstractCilnstruction {
    public CilLdcR4Instruction(ProtoIlInstructions.IlInstructionMsg instruction, DotnetBody dotnetBody, CilBlock cilBlock) {
        super(instruction, dotnetBody, cilBlock);
    }

    @Override
    public void jimplify(Body jb) {
        throw new NoStatementInstructionException(instruction);
    }

    @Override
    public Value jimplifyExpr(Body jb) {
        return FloatConstant.v(instruction.getValueConstantFloat());
    }
}
