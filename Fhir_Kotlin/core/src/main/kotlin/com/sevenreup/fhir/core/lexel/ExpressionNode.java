package com.sevenreup.fhir.core.lexel;

public class ExpressionNode {
    public enum Operation {
        Equals, Equivalent, NotEquals, NotEquivalent, LessThan, Greater, LessOrEqual, GreaterOrEqual, Is, As, Union, Or, And, Xor, Implies,
        Times, DivideBy, Plus, Minus, Concatenate, Div, Mod, In, Contains, MemberOf;

        public static Operation fromCode(String name) {
            if (Utilities.noString(name))
                return null;
            if (name.equals("="))
                return Operation.Equals;
            if (name.equals("~"))
                return Operation.Equivalent;
            if (name.equals("!="))
                return Operation.NotEquals;
            if (name.equals("!~"))
                return Operation.NotEquivalent;
            if (name.equals(">"))
                return Operation.Greater;
            if (name.equals("<"))
                return Operation.LessThan;
            if (name.equals(">="))
                return Operation.GreaterOrEqual;
            if (name.equals("<="))
                return Operation.LessOrEqual;
            if (name.equals("|"))
                return Operation.Union;
            if (name.equals("or"))
                return Operation.Or;
            if (name.equals("and"))
                return Operation.And;
            if (name.equals("xor"))
                return Operation.Xor;
            if (name.equals("is"))
                return Operation.Is;
            if (name.equals("as"))
                return Operation.As;
            if (name.equals("*"))
                return Operation.Times;
            if (name.equals("/"))
                return Operation.DivideBy;
            if (name.equals("+"))
                return Operation.Plus;
            if (name.equals("-"))
                return Operation.Minus;
            if (name.equals("&"))
                return Operation.Concatenate;
            if (name.equals("implies"))
                return Operation.Implies;
            if (name.equals("div"))
                return Operation.Div;
            if (name.equals("mod"))
                return Operation.Mod;
            if (name.equals("in"))
                return Operation.In;
            if (name.equals("contains"))
                return Operation.Contains;
            if (name.equals("memberOf"))
                return Operation.MemberOf;
            return null;

        }

        public String toCode() {
            switch (this) {
                case Equals:
                    return "=";
                case Equivalent:
                    return "~";
                case NotEquals:
                    return "!=";
                case NotEquivalent:
                    return "!~";
                case Greater:
                    return ">";
                case LessThan:
                    return "<";
                case GreaterOrEqual:
                    return ">=";
                case LessOrEqual:
                    return "<=";
                case Union:
                    return "|";
                case Or:
                    return "or";
                case And:
                    return "and";
                case Xor:
                    return "xor";
                case Times:
                    return "*";
                case DivideBy:
                    return "/";
                case Plus:
                    return "+";
                case Minus:
                    return "-";
                case Concatenate:
                    return "&";
                case Implies:
                    return "implies";
                case Is:
                    return "is";
                case As:
                    return "as";
                case Div:
                    return "div";
                case Mod:
                    return "mod";
                case In:
                    return "in";
                case Contains:
                    return "contains";
                case MemberOf:
                    return "memberOf";
                default:
                    return "??";
            }
        }
    }
}
