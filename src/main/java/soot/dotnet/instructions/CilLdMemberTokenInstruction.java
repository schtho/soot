package soot.dotnet.instructions;

import soot.*;
import soot.dotnet.exceptions.NoStatementInstructionException;
import soot.dotnet.members.DotnetMethod;
import soot.dotnet.members.method.DotnetBody;
import soot.dotnet.proto.ProtoAssemblyAllTypes;
import soot.dotnet.proto.ProtoIlInstructions;
import soot.dotnet.types.DotnetTypeFactory;
import soot.jimple.MethodHandle;
import soot.jimple.StringConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * ldtoken was split up by ILspy: LdMemberToken for Method and Field handles and LdTypeToken
 * Load a method/field handle token (e.g. reflection)
 */
public class CilLdMemberTokenInstruction extends AbstractCilnstruction {
    public CilLdMemberTokenInstruction(ProtoIlInstructions.IlInstructionMsg instruction, DotnetBody dotnetBody, CilBlock cilBlock) {
        super(instruction, dotnetBody, cilBlock);
    }

    @Override
    public void jimplify(Body jb) {
        throw new NoStatementInstructionException(instruction);
    }

    @Override
    public Value jimplifyExpr(Body jb) {
        if (instruction.hasField()) {
            ProtoAssemblyAllTypes.FieldDefinition field = instruction.getField();
            SootClass declaringClass = SootResolver.v().makeClassRef(field.getDeclaringType().getFullname());

            SootFieldRef sootFieldRef = Scene.v().makeFieldRef(declaringClass, field.getName(), DotnetTypeFactory.toSootType(field.getType()), field.getIsStatic());

            int kind = field.getIsStatic() ? MethodHandle.Kind.REF_GET_FIELD_STATIC.getValue() : MethodHandle.Kind.REF_GET_FIELD.getValue();
            return MethodHandle.v(sootFieldRef, kind);
        }
        else if (instruction.hasMethod()) {
            final ProtoAssemblyAllTypes.MethodDefinition method = instruction.getMethod();
            SootClass declaringClass = SootResolver.v().makeClassRef(method.getDeclaringType().getFullname());

            String methodName = method.getName();
            if (methodName.trim().isEmpty())
                throw new RuntimeException("Opcode: " + instruction.getOpCode() + ": Given method " + instruction.getMethod().getName() + " of declared type " +
                        instruction.getMethod().getDeclaringType().getFullname() +
                        " has no method name!");
            if (DotnetMethod.hasGenericRefParameters(method.getParameterList()))
                methodName = DotnetMethod.convertGenRefMethodName(methodName, method.getParameterList());

            List<Type> paramTypes = new ArrayList<>();
            for (ProtoAssemblyAllTypes.ParameterDefinition parameterDefinition : method.getParameterList())
                paramTypes.add(DotnetTypeFactory.toSootType(parameterDefinition.getType()));

            SootMethodRef methodRef = Scene.v().makeMethodRef(declaringClass, DotnetMethod.convertCtorName(methodName), paramTypes,
                    DotnetTypeFactory.toSootType(method.getReturnType()), method.getIsStatic());

            int kind;
            if (method.getIsConstructor())
                kind = MethodHandle.Kind.REF_INVOKE_CONSTRUCTOR.getValue();
            else if (method.getIsStatic())
                kind = MethodHandle.Kind.REF_INVOKE_STATIC.getValue();
            else if (declaringClass.isInterface())
                kind = MethodHandle.Kind.REF_INVOKE_INTERFACE.getValue();
            else
                kind = MethodHandle.Kind.REF_INVOKE_VIRTUAL.getValue();
            return MethodHandle.v(methodRef, kind);
        }
        else
            return StringConstant.v(instruction.getValueConstantString());
    }
}
