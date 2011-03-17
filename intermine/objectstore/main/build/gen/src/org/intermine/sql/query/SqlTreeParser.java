// $ANTLR 2.7.6 (20060327): "intermine_sql.g" -> "SqlTreeParser.java"$

package org.intermine.sql.query;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


public class SqlTreeParser extends antlr.TreeParser       implements SqlTokenTypes
 {
public SqlTreeParser() {
	tokenNames = _tokenNames;
}

	public final void start_rule(AST _t) throws RecognitionException {
		
		AST start_rule_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST start_rule_AST = null;
		
		sql(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		start_rule_AST = (AST)currentAST.root;
		returnAST = start_rule_AST;
		_retTree = _t;
	}
	
	public final void sql(AST _t) throws RecognitionException {
		
		AST sql_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST sql_AST = null;
		
		sql_statement(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop4:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==SQL_STATEMENT)) {
				sql_statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop4;
			}
			
		} while (true);
		}
		sql_AST = (AST)currentAST.root;
		returnAST = sql_AST;
		_retTree = _t;
	}
	
	public final void sql_statement(AST _t) throws RecognitionException {
		
		AST sql_statement_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST sql_statement_AST = null;
		
		AST __t6 = _t;
		AST tmp1_AST = null;
		AST tmp1_AST_in = null;
		tmp1_AST = astFactory.create((AST)_t);
		tmp1_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp1_AST);
		ASTPair __currentAST6 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SQL_STATEMENT);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_explain:
		{
			AST tmp2_AST = null;
			AST tmp2_AST_in = null;
			tmp2_AST = astFactory.create((AST)_t);
			tmp2_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp2_AST);
			match(_t,LITERAL_explain);
			_t = _t.getNextSibling();
			break;
		}
		case SELECT_LIST:
		case LITERAL_distinct:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_distinct:
		{
			AST tmp3_AST = null;
			AST tmp3_AST_in = null;
			tmp3_AST = astFactory.create((AST)_t);
			tmp3_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			match(_t,LITERAL_distinct);
			_t = _t.getNextSibling();
			break;
		}
		case SELECT_LIST:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		select_list(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case FROM_LIST:
		{
			from_list(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case WHERE_CLAUSE:
			{
				where_clause(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			case GROUP_CLAUSE:
			case ORDER_CLAUSE:
			case LIMIT_CLAUSE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case GROUP_CLAUSE:
			{
				group_clause(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case HAVING_CLAUSE:
				{
					having_clause(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case 3:
				case ORDER_CLAUSE:
				case LIMIT_CLAUSE:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				break;
			}
			case 3:
			case ORDER_CLAUSE:
			case LIMIT_CLAUSE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ORDER_CLAUSE:
			{
				order_clause(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			case LIMIT_CLAUSE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LIMIT_CLAUSE:
			{
				limit_clause(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST6;
		_t = __t6;
		_t = _t.getNextSibling();
		sql_statement_AST = (AST)currentAST.root;
		returnAST = sql_statement_AST;
		_retTree = _t;
	}
	
	public final void select_list(AST _t) throws RecognitionException {
		
		AST select_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST select_list_AST = null;
		
		AST __t16 = _t;
		AST tmp4_AST = null;
		AST tmp4_AST_in = null;
		tmp4_AST = astFactory.create((AST)_t);
		tmp4_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp4_AST);
		ASTPair __currentAST16 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SELECT_LIST);
		_t = _t.getFirstChild();
		{
		int _cnt18=0;
		_loop18:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==SELECT_VALUE)) {
				select_value(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt18>=1 ) { break _loop18; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt18++;
		} while (true);
		}
		currentAST = __currentAST16;
		_t = __t16;
		_t = _t.getNextSibling();
		select_list_AST = (AST)currentAST.root;
		returnAST = select_list_AST;
		_retTree = _t;
	}
	
	public final void from_list(AST _t) throws RecognitionException {
		
		AST from_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST from_list_AST = null;
		
		AST __t20 = _t;
		AST tmp5_AST = null;
		AST tmp5_AST_in = null;
		tmp5_AST = astFactory.create((AST)_t);
		tmp5_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp5_AST);
		ASTPair __currentAST20 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FROM_LIST);
		_t = _t.getFirstChild();
		{
		int _cnt22=0;
		_loop22:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==TABLE||_t.getType()==SUBQUERY)) {
				abstract_table(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt22>=1 ) { break _loop22; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt22++;
		} while (true);
		}
		currentAST = __currentAST20;
		_t = __t20;
		_t = _t.getNextSibling();
		from_list_AST = (AST)currentAST.root;
		returnAST = from_list_AST;
		_retTree = _t;
	}
	
	public final void where_clause(AST _t) throws RecognitionException {
		
		AST where_clause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST where_clause_AST = null;
		
		AST __t24 = _t;
		AST tmp6_AST = null;
		AST tmp6_AST_in = null;
		tmp6_AST = astFactory.create((AST)_t);
		tmp6_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp6_AST);
		ASTPair __currentAST24 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,WHERE_CLAUSE);
		_t = _t.getFirstChild();
		{
		int _cnt26=0;
		_loop26:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
				abstract_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt26>=1 ) { break _loop26; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt26++;
		} while (true);
		}
		currentAST = __currentAST24;
		_t = __t24;
		_t = _t.getNextSibling();
		where_clause_AST = (AST)currentAST.root;
		returnAST = where_clause_AST;
		_retTree = _t;
	}
	
	public final void group_clause(AST _t) throws RecognitionException {
		
		AST group_clause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST group_clause_AST = null;
		
		AST __t28 = _t;
		AST tmp7_AST = null;
		AST tmp7_AST_in = null;
		tmp7_AST = astFactory.create((AST)_t);
		tmp7_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp7_AST);
		ASTPair __currentAST28 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,GROUP_CLAUSE);
		_t = _t.getFirstChild();
		{
		int _cnt30=0;
		_loop30:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_0.member(_t.getType()))) {
				abstract_value(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt30>=1 ) { break _loop30; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt30++;
		} while (true);
		}
		currentAST = __currentAST28;
		_t = __t28;
		_t = _t.getNextSibling();
		group_clause_AST = (AST)currentAST.root;
		returnAST = group_clause_AST;
		_retTree = _t;
	}
	
	public final void having_clause(AST _t) throws RecognitionException {
		
		AST having_clause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST having_clause_AST = null;
		
		AST __t32 = _t;
		AST tmp8_AST = null;
		AST tmp8_AST_in = null;
		tmp8_AST = astFactory.create((AST)_t);
		tmp8_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp8_AST);
		ASTPair __currentAST32 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,HAVING_CLAUSE);
		_t = _t.getFirstChild();
		{
		int _cnt34=0;
		_loop34:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
				abstract_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt34>=1 ) { break _loop34; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt34++;
		} while (true);
		}
		currentAST = __currentAST32;
		_t = __t32;
		_t = _t.getNextSibling();
		having_clause_AST = (AST)currentAST.root;
		returnAST = having_clause_AST;
		_retTree = _t;
	}
	
	public final void order_clause(AST _t) throws RecognitionException {
		
		AST order_clause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST order_clause_AST = null;
		
		AST __t36 = _t;
		AST tmp9_AST = null;
		AST tmp9_AST_in = null;
		tmp9_AST = astFactory.create((AST)_t);
		tmp9_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp9_AST);
		ASTPair __currentAST36 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,ORDER_CLAUSE);
		_t = _t.getFirstChild();
		{
		int _cnt38=0;
		_loop38:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_1.member(_t.getType()))) {
				orderby_value(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt38>=1 ) { break _loop38; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt38++;
		} while (true);
		}
		currentAST = __currentAST36;
		_t = __t36;
		_t = _t.getNextSibling();
		order_clause_AST = (AST)currentAST.root;
		returnAST = order_clause_AST;
		_retTree = _t;
	}
	
	public final void limit_clause(AST _t) throws RecognitionException {
		
		AST limit_clause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST limit_clause_AST = null;
		
		AST __t40 = _t;
		AST tmp10_AST = null;
		AST tmp10_AST_in = null;
		tmp10_AST = astFactory.create((AST)_t);
		tmp10_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp10_AST);
		ASTPair __currentAST40 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,LIMIT_CLAUSE);
		_t = _t.getFirstChild();
		AST tmp11_AST = null;
		AST tmp11_AST_in = null;
		tmp11_AST = astFactory.create((AST)_t);
		tmp11_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp11_AST);
		match(_t,INTEGER);
		_t = _t.getNextSibling();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case INTEGER:
		{
			AST tmp12_AST = null;
			AST tmp12_AST_in = null;
			tmp12_AST = astFactory.create((AST)_t);
			tmp12_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp12_AST);
			match(_t,INTEGER);
			_t = _t.getNextSibling();
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST40;
		_t = __t40;
		_t = _t.getNextSibling();
		limit_clause_AST = (AST)currentAST.root;
		returnAST = limit_clause_AST;
		_retTree = _t;
	}
	
	public final void select_value(AST _t) throws RecognitionException {
		
		AST select_value_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST select_value_AST = null;
		
		AST __t43 = _t;
		AST tmp13_AST = null;
		AST tmp13_AST_in = null;
		tmp13_AST = astFactory.create((AST)_t);
		tmp13_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp13_AST);
		ASTPair __currentAST43 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SELECT_VALUE);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case FIELD_ALIAS:
		{
			field_alias(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST43;
		_t = __t43;
		_t = _t.getNextSibling();
		select_value_AST = (AST)currentAST.root;
		returnAST = select_value_AST;
		_retTree = _t;
	}
	
	public final void abstract_table(AST _t) throws RecognitionException {
		
		AST abstract_table_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abstract_table_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case TABLE:
		{
			table(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_table_AST = (AST)currentAST.root;
			break;
		}
		case SUBQUERY:
		{
			subquery(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_table_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = abstract_table_AST;
		_retTree = _t;
	}
	
	public final void abstract_constraint(AST _t) throws RecognitionException {
		
		AST abstract_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abstract_constraint_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case CONSTRAINT:
		{
			constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case NOT_CONSTRAINT:
		{
			not_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case AND_CONSTRAINT_SET:
		{
			and_constraint_set(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case OR_CONSTRAINT_SET:
		{
			or_constraint_set(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case SUBQUERY_CONSTRAINT:
		{
			subquery_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case INLIST_CONSTRAINT:
		{
			inlist_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case NULL_CONSTRAINT:
		{
			null_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = abstract_constraint_AST;
		_retTree = _t;
	}
	
	public final void abstract_value(AST _t) throws RecognitionException {
		
		AST abstract_value_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abstract_value_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case UNSAFE_FUNCTION:
		{
			unsafe_function(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value_AST = (AST)currentAST.root;
			break;
		}
		case SAFE_FUNCTION:
		{
			safe_function(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value_AST = (AST)currentAST.root;
			break;
		}
		case TYPECAST:
		{
			typecast(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value_AST = (AST)currentAST.root;
			break;
		}
		case CONSTANT:
		{
			constant(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value_AST = (AST)currentAST.root;
			break;
		}
		case FIELD:
		{
			field(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = abstract_value_AST;
		_retTree = _t;
	}
	
	public final void orderby_value(AST _t) throws RecognitionException {
		
		AST orderby_value_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST orderby_value_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ORDER_DESC:
		{
			AST __t47 = _t;
			AST tmp14_AST = null;
			AST tmp14_AST_in = null;
			tmp14_AST = astFactory.create((AST)_t);
			tmp14_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp14_AST);
			ASTPair __currentAST47 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ORDER_DESC);
			_t = _t.getFirstChild();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST47;
			_t = __t47;
			_t = _t.getNextSibling();
			orderby_value_AST = (AST)currentAST.root;
			break;
		}
		case CONSTANT:
		case FIELD:
		case SAFE_FUNCTION:
		case UNSAFE_FUNCTION:
		case TYPECAST:
		{
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			orderby_value_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = orderby_value_AST;
		_retTree = _t;
	}
	
	public final void field_alias(AST _t) throws RecognitionException {
		
		AST field_alias_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST field_alias_AST = null;
		
		AST __t50 = _t;
		AST tmp15_AST = null;
		AST tmp15_AST_in = null;
		tmp15_AST = astFactory.create((AST)_t);
		tmp15_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp15_AST);
		ASTPair __currentAST50 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FIELD_ALIAS);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case ALIAS:
		{
			AST tmp16_AST = null;
			AST tmp16_AST_in = null;
			tmp16_AST = astFactory.create((AST)_t);
			tmp16_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(_t,ALIAS);
			_t = _t.getNextSibling();
			break;
		}
		case IDENTIFIER:
		{
			AST tmp17_AST = null;
			AST tmp17_AST_in = null;
			tmp17_AST = astFactory.create((AST)_t);
			tmp17_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST50;
		_t = __t50;
		_t = _t.getNextSibling();
		field_alias_AST = (AST)currentAST.root;
		returnAST = field_alias_AST;
		_retTree = _t;
	}
	
	public final void table(AST _t) throws RecognitionException {
		
		AST table_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST table_AST = null;
		
		AST __t53 = _t;
		AST tmp18_AST = null;
		AST tmp18_AST_in = null;
		tmp18_AST = astFactory.create((AST)_t);
		tmp18_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp18_AST);
		ASTPair __currentAST53 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TABLE);
		_t = _t.getFirstChild();
		table_name(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case TABLE_ALIAS:
		{
			table_alias(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST53;
		_t = __t53;
		_t = _t.getNextSibling();
		table_AST = (AST)currentAST.root;
		returnAST = table_AST;
		_retTree = _t;
	}
	
	public final void subquery(AST _t) throws RecognitionException {
		
		AST subquery_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST subquery_AST = null;
		
		AST __t56 = _t;
		AST tmp19_AST = null;
		AST tmp19_AST_in = null;
		tmp19_AST = astFactory.create((AST)_t);
		tmp19_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp19_AST);
		ASTPair __currentAST56 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SUBQUERY);
		_t = _t.getFirstChild();
		sql(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		table_alias(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST56;
		_t = __t56;
		_t = _t.getNextSibling();
		subquery_AST = (AST)currentAST.root;
		returnAST = subquery_AST;
		_retTree = _t;
	}
	
	public final void unsafe_function(AST _t) throws RecognitionException {
		
		AST unsafe_function_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unsafe_function_AST = null;
		
		AST __t76 = _t;
		AST tmp20_AST = null;
		AST tmp20_AST_in = null;
		tmp20_AST = astFactory.create((AST)_t);
		tmp20_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp20_AST);
		ASTPair __currentAST76 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,UNSAFE_FUNCTION);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		int _cnt79=0;
		_loop79:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= PLUS && _t.getType() <= MINUS))) {
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case PLUS:
				{
					AST tmp21_AST = null;
					AST tmp21_AST_in = null;
					tmp21_AST = astFactory.create((AST)_t);
					tmp21_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp21_AST);
					match(_t,PLUS);
					_t = _t.getNextSibling();
					break;
				}
				case PERCENT:
				{
					AST tmp22_AST = null;
					AST tmp22_AST_in = null;
					tmp22_AST = astFactory.create((AST)_t);
					tmp22_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp22_AST);
					match(_t,PERCENT);
					_t = _t.getNextSibling();
					break;
				}
				case ASTERISK:
				{
					AST tmp23_AST = null;
					AST tmp23_AST_in = null;
					tmp23_AST = astFactory.create((AST)_t);
					tmp23_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp23_AST);
					match(_t,ASTERISK);
					_t = _t.getNextSibling();
					break;
				}
				case DIVIDE:
				{
					AST tmp24_AST = null;
					AST tmp24_AST_in = null;
					tmp24_AST = astFactory.create((AST)_t);
					tmp24_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp24_AST);
					match(_t,DIVIDE);
					_t = _t.getNextSibling();
					break;
				}
				case POWER:
				{
					AST tmp25_AST = null;
					AST tmp25_AST_in = null;
					tmp25_AST = astFactory.create((AST)_t);
					tmp25_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp25_AST);
					match(_t,POWER);
					_t = _t.getNextSibling();
					break;
				}
				case MINUS:
				{
					AST tmp26_AST = null;
					AST tmp26_AST_in = null;
					tmp26_AST = astFactory.create((AST)_t);
					tmp26_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp26_AST);
					match(_t,MINUS);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				abstract_value(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt79>=1 ) { break _loop79; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt79++;
		} while (true);
		}
		currentAST = __currentAST76;
		_t = __t76;
		_t = _t.getNextSibling();
		unsafe_function_AST = (AST)currentAST.root;
		returnAST = unsafe_function_AST;
		_retTree = _t;
	}
	
	public final void safe_function(AST _t) throws RecognitionException {
		
		AST safe_function_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST safe_function_AST = null;
		
		AST __t72 = _t;
		AST tmp27_AST = null;
		AST tmp27_AST_in = null;
		tmp27_AST = astFactory.create((AST)_t);
		tmp27_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp27_AST);
		ASTPair __currentAST72 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SAFE_FUNCTION);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_count:
		{
			AST tmp28_AST = null;
			AST tmp28_AST_in = null;
			tmp28_AST = astFactory.create((AST)_t);
			tmp28_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp28_AST);
			match(_t,LITERAL_count);
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_max:
		{
			AST tmp29_AST = null;
			AST tmp29_AST_in = null;
			tmp29_AST = astFactory.create((AST)_t);
			tmp29_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
			match(_t,LITERAL_max);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_min:
		{
			AST tmp30_AST = null;
			AST tmp30_AST_in = null;
			tmp30_AST = astFactory.create((AST)_t);
			tmp30_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp30_AST);
			match(_t,LITERAL_min);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_sum:
		{
			AST tmp31_AST = null;
			AST tmp31_AST_in = null;
			tmp31_AST = astFactory.create((AST)_t);
			tmp31_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp31_AST);
			match(_t,LITERAL_sum);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_avg:
		{
			AST tmp32_AST = null;
			AST tmp32_AST_in = null;
			tmp32_AST = astFactory.create((AST)_t);
			tmp32_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp32_AST);
			match(_t,LITERAL_avg);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_lower:
		{
			AST tmp33_AST = null;
			AST tmp33_AST_in = null;
			tmp33_AST = astFactory.create((AST)_t);
			tmp33_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp33_AST);
			match(_t,LITERAL_lower);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_upper:
		{
			AST tmp34_AST = null;
			AST tmp34_AST_in = null;
			tmp34_AST = astFactory.create((AST)_t);
			tmp34_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp34_AST);
			match(_t,LITERAL_upper);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_strpos:
		{
			AST tmp35_AST = null;
			AST tmp35_AST_in = null;
			tmp35_AST = astFactory.create((AST)_t);
			tmp35_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp35_AST);
			match(_t,LITERAL_strpos);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_substr:
		{
			AST tmp36_AST = null;
			AST tmp36_AST_in = null;
			tmp36_AST = astFactory.create((AST)_t);
			tmp36_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp36_AST);
			match(_t,LITERAL_substr);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CONSTANT:
			case FIELD:
			case SAFE_FUNCTION:
			case UNSAFE_FUNCTION:
			case TYPECAST:
			{
				abstract_value(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			break;
		}
		case LITERAL_coalesce:
		{
			AST tmp37_AST = null;
			AST tmp37_AST_in = null;
			tmp37_AST = astFactory.create((AST)_t);
			tmp37_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp37_AST);
			match(_t,LITERAL_coalesce);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_stddev:
		{
			AST tmp38_AST = null;
			AST tmp38_AST_in = null;
			tmp38_AST = astFactory.create((AST)_t);
			tmp38_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp38_AST);
			match(_t,LITERAL_stddev);
			_t = _t.getNextSibling();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST72;
		_t = __t72;
		_t = _t.getNextSibling();
		safe_function_AST = (AST)currentAST.root;
		returnAST = safe_function_AST;
		_retTree = _t;
	}
	
	public final void typecast(AST _t) throws RecognitionException {
		
		AST typecast_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typecast_AST = null;
		
		AST __t65 = _t;
		AST tmp39_AST = null;
		AST tmp39_AST_in = null;
		tmp39_AST = astFactory.create((AST)_t);
		tmp39_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp39_AST);
		ASTPair __currentAST65 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TYPECAST);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		int _cnt67=0;
		_loop67:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_boolean:
			{
				AST tmp40_AST = null;
				AST tmp40_AST_in = null;
				tmp40_AST = astFactory.create((AST)_t);
				tmp40_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp40_AST);
				match(_t,LITERAL_boolean);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_real:
			{
				AST tmp41_AST = null;
				AST tmp41_AST_in = null;
				tmp41_AST = astFactory.create((AST)_t);
				tmp41_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp41_AST);
				match(_t,LITERAL_real);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_double:
			{
				AST tmp42_AST = null;
				AST tmp42_AST_in = null;
				tmp42_AST = astFactory.create((AST)_t);
				tmp42_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp42_AST);
				match(_t,LITERAL_double);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_smallint:
			{
				AST tmp43_AST = null;
				AST tmp43_AST_in = null;
				tmp43_AST = astFactory.create((AST)_t);
				tmp43_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp43_AST);
				match(_t,LITERAL_smallint);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_integer:
			{
				AST tmp44_AST = null;
				AST tmp44_AST_in = null;
				tmp44_AST = astFactory.create((AST)_t);
				tmp44_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp44_AST);
				match(_t,LITERAL_integer);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_bigint:
			{
				AST tmp45_AST = null;
				AST tmp45_AST_in = null;
				tmp45_AST = astFactory.create((AST)_t);
				tmp45_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp45_AST);
				match(_t,LITERAL_bigint);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_numeric:
			{
				AST tmp46_AST = null;
				AST tmp46_AST_in = null;
				tmp46_AST = astFactory.create((AST)_t);
				tmp46_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp46_AST);
				match(_t,LITERAL_numeric);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_text:
			{
				AST tmp47_AST = null;
				AST tmp47_AST_in = null;
				tmp47_AST = astFactory.create((AST)_t);
				tmp47_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp47_AST);
				match(_t,LITERAL_text);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				if ( _cnt67>=1 ) { break _loop67; } else {throw new NoViableAltException(_t);}
			}
			}
			_cnt67++;
		} while (true);
		}
		currentAST = __currentAST65;
		_t = __t65;
		_t = _t.getNextSibling();
		typecast_AST = (AST)currentAST.root;
		returnAST = typecast_AST;
		_retTree = _t;
	}
	
	public final void constant(AST _t) throws RecognitionException {
		
		AST constant_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constant_AST = null;
		
		AST __t62 = _t;
		AST tmp48_AST = null;
		AST tmp48_AST_in = null;
		tmp48_AST = astFactory.create((AST)_t);
		tmp48_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp48_AST);
		ASTPair __currentAST62 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,CONSTANT);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case QUOTED_STRING:
		{
			AST tmp49_AST = null;
			AST tmp49_AST_in = null;
			tmp49_AST = astFactory.create((AST)_t);
			tmp49_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp49_AST);
			match(_t,QUOTED_STRING);
			_t = _t.getNextSibling();
			break;
		}
		case ESCAPED_STRING:
		{
			AST tmp50_AST = null;
			AST tmp50_AST_in = null;
			tmp50_AST = astFactory.create((AST)_t);
			tmp50_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp50_AST);
			match(_t,ESCAPED_STRING);
			_t = _t.getNextSibling();
			break;
		}
		case INTEGER:
		{
			AST tmp51_AST = null;
			AST tmp51_AST_in = null;
			tmp51_AST = astFactory.create((AST)_t);
			tmp51_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp51_AST);
			match(_t,INTEGER);
			_t = _t.getNextSibling();
			break;
		}
		case FLOAT:
		{
			AST tmp52_AST = null;
			AST tmp52_AST_in = null;
			tmp52_AST = astFactory.create((AST)_t);
			tmp52_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp52_AST);
			match(_t,FLOAT);
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_true:
		{
			AST tmp53_AST = null;
			AST tmp53_AST_in = null;
			tmp53_AST = astFactory.create((AST)_t);
			tmp53_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp53_AST);
			match(_t,LITERAL_true);
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_false:
		{
			AST tmp54_AST = null;
			AST tmp54_AST_in = null;
			tmp54_AST = astFactory.create((AST)_t);
			tmp54_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp54_AST);
			match(_t,LITERAL_false);
			_t = _t.getNextSibling();
			break;
		}
		case LITERAL_null:
		{
			AST tmp55_AST = null;
			AST tmp55_AST_in = null;
			tmp55_AST = astFactory.create((AST)_t);
			tmp55_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp55_AST);
			match(_t,LITERAL_null);
			_t = _t.getNextSibling();
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST62;
		_t = __t62;
		_t = _t.getNextSibling();
		constant_AST = (AST)currentAST.root;
		returnAST = constant_AST;
		_retTree = _t;
	}
	
	public final void field(AST _t) throws RecognitionException {
		
		AST field_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST field_AST = null;
		
		AST __t69 = _t;
		AST tmp56_AST = null;
		AST tmp56_AST_in = null;
		tmp56_AST = astFactory.create((AST)_t);
		tmp56_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp56_AST);
		ASTPair __currentAST69 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FIELD);
		_t = _t.getFirstChild();
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case TABLE_ALIAS:
		{
			table_alias(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case FIELD_NAME:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		field_name(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST69;
		_t = __t69;
		_t = _t.getNextSibling();
		field_AST = (AST)currentAST.root;
		returnAST = field_AST;
		_retTree = _t;
	}
	
	public final void table_name(AST _t) throws RecognitionException {
		
		AST table_name_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST table_name_AST = null;
		
		AST __t58 = _t;
		AST tmp57_AST = null;
		AST tmp57_AST_in = null;
		tmp57_AST = astFactory.create((AST)_t);
		tmp57_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp57_AST);
		ASTPair __currentAST58 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TABLE_NAME);
		_t = _t.getFirstChild();
		AST tmp58_AST = null;
		AST tmp58_AST_in = null;
		tmp58_AST = astFactory.create((AST)_t);
		tmp58_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp58_AST);
		match(_t,IDENTIFIER);
		_t = _t.getNextSibling();
		currentAST = __currentAST58;
		_t = __t58;
		_t = _t.getNextSibling();
		table_name_AST = (AST)currentAST.root;
		returnAST = table_name_AST;
		_retTree = _t;
	}
	
	public final void table_alias(AST _t) throws RecognitionException {
		
		AST table_alias_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST table_alias_AST = null;
		
		AST __t60 = _t;
		AST tmp59_AST = null;
		AST tmp59_AST_in = null;
		tmp59_AST = astFactory.create((AST)_t);
		tmp59_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp59_AST);
		ASTPair __currentAST60 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,TABLE_ALIAS);
		_t = _t.getFirstChild();
		AST tmp60_AST = null;
		AST tmp60_AST_in = null;
		tmp60_AST = astFactory.create((AST)_t);
		tmp60_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp60_AST);
		match(_t,IDENTIFIER);
		_t = _t.getNextSibling();
		currentAST = __currentAST60;
		_t = __t60;
		_t = _t.getNextSibling();
		table_alias_AST = (AST)currentAST.root;
		returnAST = table_alias_AST;
		_retTree = _t;
	}
	
	public final void field_name(AST _t) throws RecognitionException {
		
		AST field_name_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST field_name_AST = null;
		
		AST __t81 = _t;
		AST tmp61_AST = null;
		AST tmp61_AST_in = null;
		tmp61_AST = astFactory.create((AST)_t);
		tmp61_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp61_AST);
		ASTPair __currentAST81 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,FIELD_NAME);
		_t = _t.getFirstChild();
		AST tmp62_AST = null;
		AST tmp62_AST_in = null;
		tmp62_AST = astFactory.create((AST)_t);
		tmp62_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp62_AST);
		match(_t,IDENTIFIER);
		_t = _t.getNextSibling();
		currentAST = __currentAST81;
		_t = __t81;
		_t = _t.getNextSibling();
		field_name_AST = (AST)currentAST.root;
		returnAST = field_name_AST;
		_retTree = _t;
	}
	
	public final void constraint(AST _t) throws RecognitionException {
		
		AST constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constraint_AST = null;
		AST aleft_AST = null;
		AST aleft = null;
		AST aright_AST = null;
		AST aright = null;
		AST bleft_AST = null;
		AST bleft = null;
		AST bright_AST = null;
		AST bright = null;
		AST cleft_AST = null;
		AST cleft = null;
		AST cright_AST = null;
		AST cright = null;
		AST dleft_AST = null;
		AST dleft = null;
		AST dright_AST = null;
		AST dright = null;
		AST eleft_AST = null;
		AST eleft = null;
		AST elike = null;
		AST elike_AST = null;
		AST eright_AST = null;
		AST eright = null;
		
		boolean synPredMatched86 = false;
		if (_t==null) _t=ASTNULL;
		if (((_t.getType()==CONSTRAINT))) {
			AST __t86 = _t;
			synPredMatched86 = true;
			inputState.guessing++;
			try {
				{
				AST __t85 = _t;
				ASTPair __currentAST85 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,CONSTRAINT);
				_t = _t.getFirstChild();
				abstract_value(_t);
				_t = _retTree;
				match(_t,NOT_EQ);
				_t = _t.getNextSibling();
				currentAST = __currentAST85;
				_t = __t85;
				_t = _t.getNextSibling();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched86 = false;
			}
			_t = __t86;
inputState.guessing--;
		}
		if ( synPredMatched86 ) {
			AST __t87 = _t;
			AST tmp63_AST = null;
			AST tmp63_AST_in = null;
			tmp63_AST = astFactory.create((AST)_t);
			tmp63_AST_in = (AST)_t;
			ASTPair __currentAST87 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CONSTRAINT);
			_t = _t.getFirstChild();
			aleft = _t==ASTNULL ? null : (AST)_t;
			abstract_value(_t);
			_t = _retTree;
			aleft_AST = (AST)returnAST;
			AST tmp64_AST = null;
			AST tmp64_AST_in = null;
			tmp64_AST = astFactory.create((AST)_t);
			tmp64_AST_in = (AST)_t;
			match(_t,NOT_EQ);
			_t = _t.getNextSibling();
			aright = _t==ASTNULL ? null : (AST)_t;
			abstract_value(_t);
			_t = _retTree;
			aright_AST = (AST)returnAST;
			currentAST = __currentAST87;
			_t = __t87;
			_t = _t.getNextSibling();
			if ( inputState.guessing==0 ) {
				constraint_AST = (AST)currentAST.root;
				constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(tmp63_AST)).add(aleft_AST).add(astFactory.create(EQ,"=")).add(aright_AST))));
				currentAST.root = constraint_AST;
				currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
					constraint_AST.getFirstChild() : constraint_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched90 = false;
			if (_t==null) _t=ASTNULL;
			if (((_t.getType()==CONSTRAINT))) {
				AST __t90 = _t;
				synPredMatched90 = true;
				inputState.guessing++;
				try {
					{
					AST __t89 = _t;
					ASTPair __currentAST89 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,CONSTRAINT);
					_t = _t.getFirstChild();
					abstract_value(_t);
					_t = _retTree;
					match(_t,GE);
					_t = _t.getNextSibling();
					currentAST = __currentAST89;
					_t = __t89;
					_t = _t.getNextSibling();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched90 = false;
				}
				_t = __t90;
inputState.guessing--;
			}
			if ( synPredMatched90 ) {
				AST __t91 = _t;
				AST tmp65_AST = null;
				AST tmp65_AST_in = null;
				tmp65_AST = astFactory.create((AST)_t);
				tmp65_AST_in = (AST)_t;
				ASTPair __currentAST91 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,CONSTRAINT);
				_t = _t.getFirstChild();
				bleft = _t==ASTNULL ? null : (AST)_t;
				abstract_value(_t);
				_t = _retTree;
				bleft_AST = (AST)returnAST;
				AST tmp66_AST = null;
				AST tmp66_AST_in = null;
				tmp66_AST = astFactory.create((AST)_t);
				tmp66_AST_in = (AST)_t;
				match(_t,GE);
				_t = _t.getNextSibling();
				bright = _t==ASTNULL ? null : (AST)_t;
				abstract_value(_t);
				_t = _retTree;
				bright_AST = (AST)returnAST;
				currentAST = __currentAST91;
				_t = __t91;
				_t = _t.getNextSibling();
				if ( inputState.guessing==0 ) {
					constraint_AST = (AST)currentAST.root;
					constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(tmp65_AST)).add(bleft_AST).add(astFactory.create(LT,"<")).add(bright_AST))));
					currentAST.root = constraint_AST;
					currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
						constraint_AST.getFirstChild() : constraint_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched94 = false;
				if (_t==null) _t=ASTNULL;
				if (((_t.getType()==CONSTRAINT))) {
					AST __t94 = _t;
					synPredMatched94 = true;
					inputState.guessing++;
					try {
						{
						AST __t93 = _t;
						ASTPair __currentAST93 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,CONSTRAINT);
						_t = _t.getFirstChild();
						abstract_value(_t);
						_t = _retTree;
						match(_t,LE);
						_t = _t.getNextSibling();
						currentAST = __currentAST93;
						_t = __t93;
						_t = _t.getNextSibling();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched94 = false;
					}
					_t = __t94;
inputState.guessing--;
				}
				if ( synPredMatched94 ) {
					AST __t95 = _t;
					AST tmp67_AST = null;
					AST tmp67_AST_in = null;
					tmp67_AST = astFactory.create((AST)_t);
					tmp67_AST_in = (AST)_t;
					ASTPair __currentAST95 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,CONSTRAINT);
					_t = _t.getFirstChild();
					cleft = _t==ASTNULL ? null : (AST)_t;
					abstract_value(_t);
					_t = _retTree;
					cleft_AST = (AST)returnAST;
					AST tmp68_AST = null;
					AST tmp68_AST_in = null;
					tmp68_AST = astFactory.create((AST)_t);
					tmp68_AST_in = (AST)_t;
					match(_t,LE);
					_t = _t.getNextSibling();
					cright = _t==ASTNULL ? null : (AST)_t;
					abstract_value(_t);
					_t = _retTree;
					cright_AST = (AST)returnAST;
					currentAST = __currentAST95;
					_t = __t95;
					_t = _t.getNextSibling();
					if ( inputState.guessing==0 ) {
						constraint_AST = (AST)currentAST.root;
						constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(tmp67_AST)).add(cright_AST).add(astFactory.create(LT,"<")).add(cleft_AST))));
						currentAST.root = constraint_AST;
						currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
							constraint_AST.getFirstChild() : constraint_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					boolean synPredMatched98 = false;
					if (_t==null) _t=ASTNULL;
					if (((_t.getType()==CONSTRAINT))) {
						AST __t98 = _t;
						synPredMatched98 = true;
						inputState.guessing++;
						try {
							{
							AST __t97 = _t;
							ASTPair __currentAST97 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,CONSTRAINT);
							_t = _t.getFirstChild();
							abstract_value(_t);
							_t = _retTree;
							match(_t,GT);
							_t = _t.getNextSibling();
							currentAST = __currentAST97;
							_t = __t97;
							_t = _t.getNextSibling();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched98 = false;
						}
						_t = __t98;
inputState.guessing--;
					}
					if ( synPredMatched98 ) {
						AST __t99 = _t;
						AST tmp69_AST = null;
						AST tmp69_AST_in = null;
						tmp69_AST = astFactory.create((AST)_t);
						tmp69_AST_in = (AST)_t;
						ASTPair __currentAST99 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,CONSTRAINT);
						_t = _t.getFirstChild();
						dleft = _t==ASTNULL ? null : (AST)_t;
						abstract_value(_t);
						_t = _retTree;
						dleft_AST = (AST)returnAST;
						AST tmp70_AST = null;
						AST tmp70_AST_in = null;
						tmp70_AST = astFactory.create((AST)_t);
						tmp70_AST_in = (AST)_t;
						match(_t,GT);
						_t = _t.getNextSibling();
						dright = _t==ASTNULL ? null : (AST)_t;
						abstract_value(_t);
						_t = _retTree;
						dright_AST = (AST)returnAST;
						currentAST = __currentAST99;
						_t = __t99;
						_t = _t.getNextSibling();
						if ( inputState.guessing==0 ) {
							constraint_AST = (AST)currentAST.root;
							constraint_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(tmp69_AST)).add(dright_AST).add(astFactory.create(LT,"<")).add(dleft_AST));
							currentAST.root = constraint_AST;
							currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
								constraint_AST.getFirstChild() : constraint_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else {
						boolean synPredMatched102 = false;
						if (_t==null) _t=ASTNULL;
						if (((_t.getType()==CONSTRAINT))) {
							AST __t102 = _t;
							synPredMatched102 = true;
							inputState.guessing++;
							try {
								{
								AST __t101 = _t;
								ASTPair __currentAST101 = currentAST.copy();
								currentAST.root = currentAST.child;
								currentAST.child = null;
								match(_t,CONSTRAINT);
								_t = _t.getFirstChild();
								abstract_value(_t);
								_t = _retTree;
								match(_t,LITERAL_not);
								_t = _t.getNextSibling();
								match(_t,LITERAL_like);
								_t = _t.getNextSibling();
								currentAST = __currentAST101;
								_t = __t101;
								_t = _t.getNextSibling();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched102 = false;
							}
							_t = __t102;
inputState.guessing--;
						}
						if ( synPredMatched102 ) {
							AST __t103 = _t;
							AST tmp71_AST = null;
							AST tmp71_AST_in = null;
							tmp71_AST = astFactory.create((AST)_t);
							tmp71_AST_in = (AST)_t;
							ASTPair __currentAST103 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,CONSTRAINT);
							_t = _t.getFirstChild();
							eleft = _t==ASTNULL ? null : (AST)_t;
							abstract_value(_t);
							_t = _retTree;
							eleft_AST = (AST)returnAST;
							AST tmp72_AST_in = null;
							match(_t,LITERAL_not);
							_t = _t.getNextSibling();
							elike = (AST)_t;
							AST elike_AST_in = null;
							elike_AST = astFactory.create(elike);
							match(_t,LITERAL_like);
							_t = _t.getNextSibling();
							eright = _t==ASTNULL ? null : (AST)_t;
							abstract_value(_t);
							_t = _retTree;
							eright_AST = (AST)returnAST;
							currentAST = __currentAST103;
							_t = __t103;
							_t = _t.getNextSibling();
							if ( inputState.guessing==0 ) {
								constraint_AST = (AST)currentAST.root;
								constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(tmp71_AST)).add(eleft_AST).add(elike_AST).add(eright_AST))));
								currentAST.root = constraint_AST;
								currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
									constraint_AST.getFirstChild() : constraint_AST;
								currentAST.advanceChildToEnd();
							}
						}
						else if ((_t.getType()==CONSTRAINT)) {
							AST __t104 = _t;
							AST tmp73_AST = null;
							AST tmp73_AST_in = null;
							tmp73_AST = astFactory.create((AST)_t);
							tmp73_AST_in = (AST)_t;
							astFactory.addASTChild(currentAST, tmp73_AST);
							ASTPair __currentAST104 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,CONSTRAINT);
							_t = _t.getFirstChild();
							abstract_value(_t);
							_t = _retTree;
							astFactory.addASTChild(currentAST, returnAST);
							comparison_op(_t);
							_t = _retTree;
							astFactory.addASTChild(currentAST, returnAST);
							abstract_value(_t);
							_t = _retTree;
							astFactory.addASTChild(currentAST, returnAST);
							currentAST = __currentAST104;
							_t = __t104;
							_t = _t.getNextSibling();
							constraint_AST = (AST)currentAST.root;
						}
						else {
							throw new NoViableAltException(_t);
						}
						}}}}
						returnAST = constraint_AST;
						_retTree = _t;
					}
					
	public final void not_constraint(AST _t) throws RecognitionException {
		
		AST not_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST not_constraint_AST = null;
		AST a_AST = null;
		AST a = null;
		AST b_AST = null;
		AST b = null;
		AST c_AST = null;
		AST c = null;
		AST e_AST = null;
		AST e = null;
		AST f_AST = null;
		AST f = null;
		
		boolean synPredMatched114 = false;
		if (_t==null) _t=ASTNULL;
		if (((_t.getType()==NOT_CONSTRAINT))) {
			AST __t114 = _t;
			synPredMatched114 = true;
			inputState.guessing++;
			try {
				{
				AST __t113 = _t;
				ASTPair __currentAST113 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,NOT_CONSTRAINT);
				_t = _t.getFirstChild();
				match(_t,NOT_CONSTRAINT);
				_t = _t.getNextSibling();
				currentAST = __currentAST113;
				_t = __t113;
				_t = _t.getNextSibling();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched114 = false;
			}
			_t = __t114;
inputState.guessing--;
		}
		if ( synPredMatched114 ) {
			AST __t115 = _t;
			AST tmp74_AST = null;
			AST tmp74_AST_in = null;
			tmp74_AST = astFactory.create((AST)_t);
			tmp74_AST_in = (AST)_t;
			ASTPair __currentAST115 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NOT_CONSTRAINT);
			_t = _t.getFirstChild();
			AST __t116 = _t;
			AST tmp75_AST = null;
			AST tmp75_AST_in = null;
			tmp75_AST = astFactory.create((AST)_t);
			tmp75_AST_in = (AST)_t;
			ASTPair __currentAST116 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NOT_CONSTRAINT);
			_t = _t.getFirstChild();
			a = _t==ASTNULL ? null : (AST)_t;
			n_abstract_constraint(_t);
			_t = _retTree;
			a_AST = (AST)returnAST;
			currentAST = __currentAST116;
			_t = __t116;
			_t = _t.getNextSibling();
			currentAST = __currentAST115;
			_t = __t115;
			_t = _t.getNextSibling();
			if ( inputState.guessing==0 ) {
				not_constraint_AST = (AST)currentAST.root;
				not_constraint_AST = a_AST;
				currentAST.root = not_constraint_AST;
				currentAST.child = not_constraint_AST!=null &&not_constraint_AST.getFirstChild()!=null ?
					not_constraint_AST.getFirstChild() : not_constraint_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched120 = false;
			if (_t==null) _t=ASTNULL;
			if (((_t.getType()==NOT_CONSTRAINT))) {
				AST __t120 = _t;
				synPredMatched120 = true;
				inputState.guessing++;
				try {
					{
					AST __t118 = _t;
					ASTPair __currentAST118 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,NOT_CONSTRAINT);
					_t = _t.getFirstChild();
					AST __t119 = _t;
					ASTPair __currentAST119 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,OR_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					n_abstract_constraint(_t);
					_t = _retTree;
					n_abstract_constraint(_t);
					_t = _retTree;
					currentAST = __currentAST119;
					_t = __t119;
					_t = _t.getNextSibling();
					currentAST = __currentAST118;
					_t = __t118;
					_t = _t.getNextSibling();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched120 = false;
				}
				_t = __t120;
inputState.guessing--;
			}
			if ( synPredMatched120 ) {
				AST __t121 = _t;
				AST tmp76_AST = null;
				AST tmp76_AST_in = null;
				tmp76_AST = astFactory.create((AST)_t);
				tmp76_AST_in = (AST)_t;
				ASTPair __currentAST121 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,NOT_CONSTRAINT);
				_t = _t.getFirstChild();
				AST __t122 = _t;
				AST tmp77_AST = null;
				AST tmp77_AST_in = null;
				tmp77_AST = astFactory.create((AST)_t);
				tmp77_AST_in = (AST)_t;
				ASTPair __currentAST122 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				b = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint(_t);
				_t = _retTree;
				b_AST = (AST)returnAST;
				c = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list(_t);
				_t = _retTree;
				c_AST = (AST)returnAST;
				currentAST = __currentAST122;
				_t = __t122;
				_t = _t.getNextSibling();
				currentAST = __currentAST121;
				_t = __t121;
				_t = _t.getNextSibling();
				if ( inputState.guessing==0 ) {
					not_constraint_AST = (AST)currentAST.root;
					not_constraint_AST = (AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(AND_CONSTRAINT_SET,"AND_CONSTRAINT_SET")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp76_AST)).add(b_AST))).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp76_AST)).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp77_AST)).add(c_AST))))));
					currentAST.root = not_constraint_AST;
					currentAST.child = not_constraint_AST!=null &&not_constraint_AST.getFirstChild()!=null ?
						not_constraint_AST.getFirstChild() : not_constraint_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched126 = false;
				if (_t==null) _t=ASTNULL;
				if (((_t.getType()==NOT_CONSTRAINT))) {
					AST __t126 = _t;
					synPredMatched126 = true;
					inputState.guessing++;
					try {
						{
						AST __t124 = _t;
						ASTPair __currentAST124 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,NOT_CONSTRAINT);
						_t = _t.getFirstChild();
						AST __t125 = _t;
						ASTPair __currentAST125 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,AND_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						n_abstract_constraint(_t);
						_t = _retTree;
						n_abstract_constraint(_t);
						_t = _retTree;
						currentAST = __currentAST125;
						_t = __t125;
						_t = _t.getNextSibling();
						currentAST = __currentAST124;
						_t = __t124;
						_t = _t.getNextSibling();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched126 = false;
					}
					_t = __t126;
inputState.guessing--;
				}
				if ( synPredMatched126 ) {
					AST __t127 = _t;
					AST tmp78_AST = null;
					AST tmp78_AST_in = null;
					tmp78_AST = astFactory.create((AST)_t);
					tmp78_AST_in = (AST)_t;
					ASTPair __currentAST127 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,NOT_CONSTRAINT);
					_t = _t.getFirstChild();
					AST __t128 = _t;
					AST tmp79_AST = null;
					AST tmp79_AST_in = null;
					tmp79_AST = astFactory.create((AST)_t);
					tmp79_AST_in = (AST)_t;
					ASTPair __currentAST128 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,AND_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					e = _t==ASTNULL ? null : (AST)_t;
					n_abstract_constraint(_t);
					_t = _retTree;
					e_AST = (AST)returnAST;
					f = _t==ASTNULL ? null : (AST)_t;
					n_abstract_constraint_list(_t);
					_t = _retTree;
					f_AST = (AST)returnAST;
					currentAST = __currentAST128;
					_t = __t128;
					_t = _t.getNextSibling();
					currentAST = __currentAST127;
					_t = __t127;
					_t = _t.getNextSibling();
					if ( inputState.guessing==0 ) {
						not_constraint_AST = (AST)currentAST.root;
						not_constraint_AST = (AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(OR_CONSTRAINT_SET,"OR_CONSTRAINT_SET")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp78_AST)).add(e_AST))).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp78_AST)).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp79_AST)).add(f_AST))))));
						currentAST.root = not_constraint_AST;
						currentAST.child = not_constraint_AST!=null &&not_constraint_AST.getFirstChild()!=null ?
							not_constraint_AST.getFirstChild() : not_constraint_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else if ((_t.getType()==NOT_CONSTRAINT)) {
					AST __t129 = _t;
					AST tmp80_AST = null;
					AST tmp80_AST_in = null;
					tmp80_AST = astFactory.create((AST)_t);
					tmp80_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp80_AST);
					ASTPair __currentAST129 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,NOT_CONSTRAINT);
					_t = _t.getFirstChild();
					abstract_constraint(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					currentAST = __currentAST129;
					_t = __t129;
					_t = _t.getNextSibling();
					not_constraint_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(_t);
				}
				}}
				returnAST = not_constraint_AST;
				_retTree = _t;
			}
			
	public final void and_constraint_set(AST _t) throws RecognitionException {
		
		AST and_constraint_set_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST and_constraint_set_AST = null;
		AST ta = null;
		AST ta_AST = null;
		AST a_AST = null;
		AST a = null;
		AST b_AST = null;
		AST b = null;
		AST tc = null;
		AST tc_AST = null;
		AST d_AST = null;
		AST d = null;
		AST e_AST = null;
		AST e = null;
		AST f_AST = null;
		AST f = null;
		AST td = null;
		AST td_AST = null;
		AST g_AST = null;
		AST g = null;
		AST h_AST = null;
		AST h = null;
		AST te = null;
		AST te_AST = null;
		AST tf = null;
		AST tf_AST = null;
		AST z_AST = null;
		AST z = null;
		
		boolean synPredMatched174 = false;
		if (_t==null) _t=ASTNULL;
		if (((_t.getType()==AND_CONSTRAINT_SET))) {
			AST __t174 = _t;
			synPredMatched174 = true;
			inputState.guessing++;
			try {
				{
				AST __t173 = _t;
				ASTPair __currentAST173 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				match(_t,AND_CONSTRAINT_SET);
				_t = _t.getNextSibling();
				n_abstract_constraint(_t);
				_t = _retTree;
				currentAST = __currentAST173;
				_t = __t173;
				_t = _t.getNextSibling();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched174 = false;
			}
			_t = __t174;
inputState.guessing--;
		}
		if ( synPredMatched174 ) {
			AST __t175 = _t;
			ta = _t==ASTNULL ? null :(AST)_t;
			AST ta_AST_in = null;
			ta_AST = astFactory.create(ta);
			ASTPair __currentAST175 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,AND_CONSTRAINT_SET);
			_t = _t.getFirstChild();
			AST __t176 = _t;
			AST tmp81_AST = null;
			AST tmp81_AST_in = null;
			tmp81_AST = astFactory.create((AST)_t);
			tmp81_AST_in = (AST)_t;
			ASTPair __currentAST176 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,AND_CONSTRAINT_SET);
			_t = _t.getFirstChild();
			a = _t==ASTNULL ? null : (AST)_t;
			n_abstract_constraint_list(_t);
			_t = _retTree;
			a_AST = (AST)returnAST;
			currentAST = __currentAST176;
			_t = __t176;
			_t = _t.getNextSibling();
			b = _t==ASTNULL ? null : (AST)_t;
			n_abstract_constraint_list(_t);
			_t = _retTree;
			b_AST = (AST)returnAST;
			currentAST = __currentAST175;
			_t = __t175;
			_t = _t.getNextSibling();
			if ( inputState.guessing==0 ) {
				and_constraint_set_AST = (AST)currentAST.root;
				and_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(ta_AST).add(a_AST).add(b_AST));
				currentAST.root = and_constraint_set_AST;
				currentAST.child = and_constraint_set_AST!=null &&and_constraint_set_AST.getFirstChild()!=null ?
					and_constraint_set_AST.getFirstChild() : and_constraint_set_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched179 = false;
			if (_t==null) _t=ASTNULL;
			if (((_t.getType()==AND_CONSTRAINT_SET))) {
				AST __t179 = _t;
				synPredMatched179 = true;
				inputState.guessing++;
				try {
					{
					AST __t178 = _t;
					ASTPair __currentAST178 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,AND_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					n_abstract_constraint_list_notand(_t);
					_t = _retTree;
					match(_t,AND_CONSTRAINT_SET);
					_t = _t.getNextSibling();
					n_abstract_constraint(_t);
					_t = _retTree;
					currentAST = __currentAST178;
					_t = __t178;
					_t = _t.getNextSibling();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched179 = false;
				}
				_t = __t179;
inputState.guessing--;
			}
			if ( synPredMatched179 ) {
				AST __t180 = _t;
				tc = _t==ASTNULL ? null :(AST)_t;
				AST tc_AST_in = null;
				tc_AST = astFactory.create(tc);
				ASTPair __currentAST180 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				d = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list_notand(_t);
				_t = _retTree;
				d_AST = (AST)returnAST;
				AST __t181 = _t;
				AST tmp82_AST = null;
				AST tmp82_AST_in = null;
				tmp82_AST = astFactory.create((AST)_t);
				tmp82_AST_in = (AST)_t;
				ASTPair __currentAST181 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,AND_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				e = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list(_t);
				_t = _retTree;
				e_AST = (AST)returnAST;
				currentAST = __currentAST181;
				_t = __t181;
				_t = _t.getNextSibling();
				f = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list(_t);
				_t = _retTree;
				f_AST = (AST)returnAST;
				currentAST = __currentAST180;
				_t = __t180;
				_t = _t.getNextSibling();
				if ( inputState.guessing==0 ) {
					and_constraint_set_AST = (AST)currentAST.root;
					and_constraint_set_AST = (AST)astFactory.make( (new ASTArray(4)).add(tc_AST).add(d_AST).add(e_AST).add(f_AST));
					currentAST.root = and_constraint_set_AST;
					currentAST.child = and_constraint_set_AST!=null &&and_constraint_set_AST.getFirstChild()!=null ?
						and_constraint_set_AST.getFirstChild() : and_constraint_set_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched184 = false;
				if (_t==null) _t=ASTNULL;
				if (((_t.getType()==AND_CONSTRAINT_SET))) {
					AST __t184 = _t;
					synPredMatched184 = true;
					inputState.guessing++;
					try {
						{
						AST __t183 = _t;
						ASTPair __currentAST183 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,AND_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						n_abstract_constraint_list_notand(_t);
						_t = _retTree;
						match(_t,AND_CONSTRAINT_SET);
						_t = _t.getNextSibling();
						currentAST = __currentAST183;
						_t = __t183;
						_t = _t.getNextSibling();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched184 = false;
					}
					_t = __t184;
inputState.guessing--;
				}
				if ( synPredMatched184 ) {
					AST __t185 = _t;
					td = _t==ASTNULL ? null :(AST)_t;
					AST td_AST_in = null;
					td_AST = astFactory.create(td);
					ASTPair __currentAST185 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,AND_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					g = _t==ASTNULL ? null : (AST)_t;
					n_abstract_constraint_list_notand(_t);
					_t = _retTree;
					g_AST = (AST)returnAST;
					AST __t186 = _t;
					AST tmp83_AST = null;
					AST tmp83_AST_in = null;
					tmp83_AST = astFactory.create((AST)_t);
					tmp83_AST_in = (AST)_t;
					ASTPair __currentAST186 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,AND_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					h = _t==ASTNULL ? null : (AST)_t;
					n_abstract_constraint_list(_t);
					_t = _retTree;
					h_AST = (AST)returnAST;
					currentAST = __currentAST186;
					_t = __t186;
					_t = _t.getNextSibling();
					currentAST = __currentAST185;
					_t = __t185;
					_t = _t.getNextSibling();
					if ( inputState.guessing==0 ) {
						and_constraint_set_AST = (AST)currentAST.root;
						and_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(td_AST).add(g_AST).add(h_AST));
						currentAST.root = and_constraint_set_AST;
						currentAST.child = and_constraint_set_AST!=null &&and_constraint_set_AST.getFirstChild()!=null ?
							and_constraint_set_AST.getFirstChild() : and_constraint_set_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					boolean synPredMatched189 = false;
					if (_t==null) _t=ASTNULL;
					if (((_t.getType()==AND_CONSTRAINT_SET))) {
						AST __t189 = _t;
						synPredMatched189 = true;
						inputState.guessing++;
						try {
							{
							AST __t188 = _t;
							ASTPair __currentAST188 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,AND_CONSTRAINT_SET);
							_t = _t.getFirstChild();
							n_abstract_constraint(_t);
							_t = _retTree;
							n_abstract_constraint(_t);
							_t = _retTree;
							currentAST = __currentAST188;
							_t = __t188;
							_t = _t.getNextSibling();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched189 = false;
						}
						_t = __t189;
inputState.guessing--;
					}
					if ( synPredMatched189 ) {
						AST __t190 = _t;
						te = _t==ASTNULL ? null :(AST)_t;
						AST te_AST_in = null;
						te_AST = astFactory.create(te);
						astFactory.addASTChild(currentAST, te_AST);
						ASTPair __currentAST190 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,AND_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						{
						int _cnt192=0;
						_loop192:
						do {
							if (_t==null) _t=ASTNULL;
							if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
								abstract_constraint(_t);
								_t = _retTree;
								astFactory.addASTChild(currentAST, returnAST);
							}
							else {
								if ( _cnt192>=1 ) { break _loop192; } else {throw new NoViableAltException(_t);}
							}
							
							_cnt192++;
						} while (true);
						}
						currentAST = __currentAST190;
						_t = __t190;
						_t = _t.getNextSibling();
						if ( inputState.guessing==0 ) {
							
						}
						and_constraint_set_AST = (AST)currentAST.root;
					}
					else if ((_t.getType()==AND_CONSTRAINT_SET)) {
						AST __t193 = _t;
						tf = _t==ASTNULL ? null :(AST)_t;
						AST tf_AST_in = null;
						tf_AST = astFactory.create(tf);
						ASTPair __currentAST193 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,AND_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						z = _t==ASTNULL ? null : (AST)_t;
						n_abstract_constraint(_t);
						_t = _retTree;
						z_AST = (AST)returnAST;
						currentAST = __currentAST193;
						_t = __t193;
						_t = _t.getNextSibling();
						if ( inputState.guessing==0 ) {
							and_constraint_set_AST = (AST)currentAST.root;
							and_constraint_set_AST = z_AST;
							currentAST.root = and_constraint_set_AST;
							currentAST.child = and_constraint_set_AST!=null &&and_constraint_set_AST.getFirstChild()!=null ?
								and_constraint_set_AST.getFirstChild() : and_constraint_set_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else {
						throw new NoViableAltException(_t);
					}
					}}}
					returnAST = and_constraint_set_AST;
					_retTree = _t;
				}
				
	public final void or_constraint_set(AST _t) throws RecognitionException {
		
		AST or_constraint_set_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST or_constraint_set_AST = null;
		AST ta = null;
		AST ta_AST = null;
		AST a_AST = null;
		AST a = null;
		AST b_AST = null;
		AST b = null;
		AST tc = null;
		AST tc_AST = null;
		AST d_AST = null;
		AST d = null;
		AST e_AST = null;
		AST e = null;
		AST f_AST = null;
		AST f = null;
		AST td = null;
		AST td_AST = null;
		AST g_AST = null;
		AST g = null;
		AST h_AST = null;
		AST h = null;
		AST te = null;
		AST te_AST = null;
		AST tf = null;
		AST tf_AST = null;
		AST i_AST = null;
		AST i = null;
		AST j_AST = null;
		AST j = null;
		AST k_AST = null;
		AST k = null;
		AST tg = null;
		AST tg_AST = null;
		AST l_AST = null;
		AST l = null;
		AST th = null;
		AST th_AST = null;
		AST m_AST = null;
		AST m = null;
		AST n_AST = null;
		AST n = null;
		AST o_AST = null;
		AST o = null;
		AST ti = null;
		AST ti_AST = null;
		AST p_AST = null;
		AST p = null;
		AST tj = null;
		AST tj_AST = null;
		AST q_AST = null;
		AST q = null;
		AST r_AST = null;
		AST r = null;
		AST z_AST = null;
		AST z = null;
		
		boolean synPredMatched133 = false;
		if (_t==null) _t=ASTNULL;
		if (((_t.getType()==OR_CONSTRAINT_SET))) {
			AST __t133 = _t;
			synPredMatched133 = true;
			inputState.guessing++;
			try {
				{
				AST __t132 = _t;
				ASTPair __currentAST132 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				match(_t,OR_CONSTRAINT_SET);
				_t = _t.getNextSibling();
				n_abstract_constraint(_t);
				_t = _retTree;
				currentAST = __currentAST132;
				_t = __t132;
				_t = _t.getNextSibling();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched133 = false;
			}
			_t = __t133;
inputState.guessing--;
		}
		if ( synPredMatched133 ) {
			AST __t134 = _t;
			ta = _t==ASTNULL ? null :(AST)_t;
			AST ta_AST_in = null;
			ta_AST = astFactory.create(ta);
			ASTPair __currentAST134 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,OR_CONSTRAINT_SET);
			_t = _t.getFirstChild();
			AST __t135 = _t;
			AST tmp84_AST = null;
			AST tmp84_AST_in = null;
			tmp84_AST = astFactory.create((AST)_t);
			tmp84_AST_in = (AST)_t;
			ASTPair __currentAST135 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,OR_CONSTRAINT_SET);
			_t = _t.getFirstChild();
			a = _t==ASTNULL ? null : (AST)_t;
			n_abstract_constraint_list(_t);
			_t = _retTree;
			a_AST = (AST)returnAST;
			currentAST = __currentAST135;
			_t = __t135;
			_t = _t.getNextSibling();
			b = _t==ASTNULL ? null : (AST)_t;
			n_abstract_constraint_list(_t);
			_t = _retTree;
			b_AST = (AST)returnAST;
			currentAST = __currentAST134;
			_t = __t134;
			_t = _t.getNextSibling();
			if ( inputState.guessing==0 ) {
				or_constraint_set_AST = (AST)currentAST.root;
				or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(ta_AST).add(a_AST).add(b_AST));
				currentAST.root = or_constraint_set_AST;
				currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
					or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched138 = false;
			if (_t==null) _t=ASTNULL;
			if (((_t.getType()==OR_CONSTRAINT_SET))) {
				AST __t138 = _t;
				synPredMatched138 = true;
				inputState.guessing++;
				try {
					{
					AST __t137 = _t;
					ASTPair __currentAST137 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,OR_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					n_abstract_constraint_list_notor(_t);
					_t = _retTree;
					match(_t,OR_CONSTRAINT_SET);
					_t = _t.getNextSibling();
					n_abstract_constraint(_t);
					_t = _retTree;
					currentAST = __currentAST137;
					_t = __t137;
					_t = _t.getNextSibling();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched138 = false;
				}
				_t = __t138;
inputState.guessing--;
			}
			if ( synPredMatched138 ) {
				AST __t139 = _t;
				tc = _t==ASTNULL ? null :(AST)_t;
				AST tc_AST_in = null;
				tc_AST = astFactory.create(tc);
				ASTPair __currentAST139 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				d = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list_notor(_t);
				_t = _retTree;
				d_AST = (AST)returnAST;
				AST __t140 = _t;
				AST tmp85_AST = null;
				AST tmp85_AST_in = null;
				tmp85_AST = astFactory.create((AST)_t);
				tmp85_AST_in = (AST)_t;
				ASTPair __currentAST140 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,OR_CONSTRAINT_SET);
				_t = _t.getFirstChild();
				e = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list(_t);
				_t = _retTree;
				e_AST = (AST)returnAST;
				currentAST = __currentAST140;
				_t = __t140;
				_t = _t.getNextSibling();
				f = _t==ASTNULL ? null : (AST)_t;
				n_abstract_constraint_list(_t);
				_t = _retTree;
				f_AST = (AST)returnAST;
				currentAST = __currentAST139;
				_t = __t139;
				_t = _t.getNextSibling();
				if ( inputState.guessing==0 ) {
					or_constraint_set_AST = (AST)currentAST.root;
					or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(4)).add(tc_AST).add(d_AST).add(e_AST).add(f_AST));
					currentAST.root = or_constraint_set_AST;
					currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
						or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched143 = false;
				if (_t==null) _t=ASTNULL;
				if (((_t.getType()==OR_CONSTRAINT_SET))) {
					AST __t143 = _t;
					synPredMatched143 = true;
					inputState.guessing++;
					try {
						{
						AST __t142 = _t;
						ASTPair __currentAST142 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,OR_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						n_abstract_constraint_list_notor(_t);
						_t = _retTree;
						match(_t,OR_CONSTRAINT_SET);
						_t = _t.getNextSibling();
						currentAST = __currentAST142;
						_t = __t142;
						_t = _t.getNextSibling();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched143 = false;
					}
					_t = __t143;
inputState.guessing--;
				}
				if ( synPredMatched143 ) {
					AST __t144 = _t;
					td = _t==ASTNULL ? null :(AST)_t;
					AST td_AST_in = null;
					td_AST = astFactory.create(td);
					ASTPair __currentAST144 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,OR_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					g = _t==ASTNULL ? null : (AST)_t;
					n_abstract_constraint_list_notor(_t);
					_t = _retTree;
					g_AST = (AST)returnAST;
					AST __t145 = _t;
					AST tmp86_AST = null;
					AST tmp86_AST_in = null;
					tmp86_AST = astFactory.create((AST)_t);
					tmp86_AST_in = (AST)_t;
					ASTPair __currentAST145 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,OR_CONSTRAINT_SET);
					_t = _t.getFirstChild();
					h = _t==ASTNULL ? null : (AST)_t;
					n_abstract_constraint_list(_t);
					_t = _retTree;
					h_AST = (AST)returnAST;
					currentAST = __currentAST145;
					_t = __t145;
					_t = _t.getNextSibling();
					currentAST = __currentAST144;
					_t = __t144;
					_t = _t.getNextSibling();
					if ( inputState.guessing==0 ) {
						or_constraint_set_AST = (AST)currentAST.root;
						or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(td_AST).add(g_AST).add(h_AST));
						currentAST.root = or_constraint_set_AST;
						currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
							or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					boolean synPredMatched149 = false;
					if (_t==null) _t=ASTNULL;
					if (((_t.getType()==OR_CONSTRAINT_SET))) {
						AST __t149 = _t;
						synPredMatched149 = true;
						inputState.guessing++;
						try {
							{
							AST __t147 = _t;
							ASTPair __currentAST147 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,OR_CONSTRAINT_SET);
							_t = _t.getFirstChild();
							AST __t148 = _t;
							ASTPair __currentAST148 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,AND_CONSTRAINT_SET);
							_t = _t.getFirstChild();
							n_abstract_constraint(_t);
							_t = _retTree;
							n_abstract_constraint_list(_t);
							_t = _retTree;
							currentAST = __currentAST148;
							_t = __t148;
							_t = _t.getNextSibling();
							n_abstract_constraint(_t);
							_t = _retTree;
							currentAST = __currentAST147;
							_t = __t147;
							_t = _t.getNextSibling();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched149 = false;
						}
						_t = __t149;
inputState.guessing--;
					}
					if ( synPredMatched149 ) {
						AST __t150 = _t;
						te = _t==ASTNULL ? null :(AST)_t;
						AST te_AST_in = null;
						te_AST = astFactory.create(te);
						ASTPair __currentAST150 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,OR_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						AST __t151 = _t;
						tf = _t==ASTNULL ? null :(AST)_t;
						AST tf_AST_in = null;
						tf_AST = astFactory.create(tf);
						ASTPair __currentAST151 = currentAST.copy();
						currentAST.root = currentAST.child;
						currentAST.child = null;
						match(_t,AND_CONSTRAINT_SET);
						_t = _t.getFirstChild();
						i = _t==ASTNULL ? null : (AST)_t;
						n_abstract_constraint(_t);
						_t = _retTree;
						i_AST = (AST)returnAST;
						j = _t==ASTNULL ? null : (AST)_t;
						n_abstract_constraint_list(_t);
						_t = _retTree;
						j_AST = (AST)returnAST;
						currentAST = __currentAST151;
						_t = __t151;
						_t = _t.getNextSibling();
						k = _t==ASTNULL ? null : (AST)_t;
						n_abstract_constraint_list(_t);
						_t = _retTree;
						k_AST = (AST)returnAST;
						currentAST = __currentAST150;
						_t = __t150;
						_t = _t.getNextSibling();
						if ( inputState.guessing==0 ) {
							or_constraint_set_AST = (AST)currentAST.root;
							AST te2_AST = astFactory.create(te);
							AST k2_AST = astFactory.dupList(k_AST);
							AST tf2_AST = astFactory.create(tf);
							or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(tf_AST).add((AST)astFactory.make( (new ASTArray(3)).add(te_AST).add(i_AST).add(k_AST))).add((AST)astFactory.make( (new ASTArray(3)).add(te2_AST).add((AST)astFactory.make( (new ASTArray(2)).add(tf2_AST).add(j_AST))).add(k2_AST))));
							
							currentAST.root = or_constraint_set_AST;
							currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
								or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else {
						boolean synPredMatched155 = false;
						if (_t==null) _t=ASTNULL;
						if (((_t.getType()==OR_CONSTRAINT_SET))) {
							AST __t155 = _t;
							synPredMatched155 = true;
							inputState.guessing++;
							try {
								{
								AST __t153 = _t;
								ASTPair __currentAST153 = currentAST.copy();
								currentAST.root = currentAST.child;
								currentAST.child = null;
								match(_t,OR_CONSTRAINT_SET);
								_t = _t.getFirstChild();
								n_abstract_constraint_list_notand(_t);
								_t = _retTree;
								AST __t154 = _t;
								ASTPair __currentAST154 = currentAST.copy();
								currentAST.root = currentAST.child;
								currentAST.child = null;
								match(_t,AND_CONSTRAINT_SET);
								_t = _t.getFirstChild();
								n_abstract_constraint(_t);
								_t = _retTree;
								n_abstract_constraint(_t);
								_t = _retTree;
								currentAST = __currentAST154;
								_t = __t154;
								_t = _t.getNextSibling();
								n_abstract_constraint_list(_t);
								_t = _retTree;
								currentAST = __currentAST153;
								_t = __t153;
								_t = _t.getNextSibling();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched155 = false;
							}
							_t = __t155;
inputState.guessing--;
						}
						if ( synPredMatched155 ) {
							AST __t156 = _t;
							tg = _t==ASTNULL ? null :(AST)_t;
							AST tg_AST_in = null;
							tg_AST = astFactory.create(tg);
							ASTPair __currentAST156 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,OR_CONSTRAINT_SET);
							_t = _t.getFirstChild();
							l = _t==ASTNULL ? null : (AST)_t;
							n_abstract_constraint_list_notand(_t);
							_t = _retTree;
							l_AST = (AST)returnAST;
							AST __t157 = _t;
							th = _t==ASTNULL ? null :(AST)_t;
							AST th_AST_in = null;
							th_AST = astFactory.create(th);
							ASTPair __currentAST157 = currentAST.copy();
							currentAST.root = currentAST.child;
							currentAST.child = null;
							match(_t,AND_CONSTRAINT_SET);
							_t = _t.getFirstChild();
							m = _t==ASTNULL ? null : (AST)_t;
							n_abstract_constraint(_t);
							_t = _retTree;
							m_AST = (AST)returnAST;
							n = _t==ASTNULL ? null : (AST)_t;
							n_abstract_constraint_list(_t);
							_t = _retTree;
							n_AST = (AST)returnAST;
							currentAST = __currentAST157;
							_t = __t157;
							_t = _t.getNextSibling();
							o = _t==ASTNULL ? null : (AST)_t;
							n_abstract_constraint_list(_t);
							_t = _retTree;
							o_AST = (AST)returnAST;
							currentAST = __currentAST156;
							_t = __t156;
							_t = _t.getNextSibling();
							if ( inputState.guessing==0 ) {
								or_constraint_set_AST = (AST)currentAST.root;
								AST tg2_AST = astFactory.create(tg);
								AST l2_AST = astFactory.dupList(l_AST);
								AST o2_AST = astFactory.dupList(o_AST);
								AST th2_AST = astFactory.create(th);
								or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(th_AST).add((AST)astFactory.make( (new ASTArray(4)).add(tg_AST).add(l_AST).add(m_AST).add(o_AST))).add((AST)astFactory.make( (new ASTArray(4)).add(tg2_AST).add(l2_AST).add((AST)astFactory.make( (new ASTArray(2)).add(th2_AST).add(n_AST))).add(o2_AST))));
								currentAST.root = or_constraint_set_AST;
								currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
									or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
								currentAST.advanceChildToEnd();
							}
						}
						else {
							boolean synPredMatched161 = false;
							if (_t==null) _t=ASTNULL;
							if (((_t.getType()==OR_CONSTRAINT_SET))) {
								AST __t161 = _t;
								synPredMatched161 = true;
								inputState.guessing++;
								try {
									{
									AST __t159 = _t;
									ASTPair __currentAST159 = currentAST.copy();
									currentAST.root = currentAST.child;
									currentAST.child = null;
									match(_t,OR_CONSTRAINT_SET);
									_t = _t.getFirstChild();
									n_abstract_constraint_list_notand(_t);
									_t = _retTree;
									AST __t160 = _t;
									ASTPair __currentAST160 = currentAST.copy();
									currentAST.root = currentAST.child;
									currentAST.child = null;
									match(_t,AND_CONSTRAINT_SET);
									_t = _t.getFirstChild();
									n_abstract_constraint(_t);
									_t = _retTree;
									n_abstract_constraint(_t);
									_t = _retTree;
									currentAST = __currentAST160;
									_t = __t160;
									_t = _t.getNextSibling();
									currentAST = __currentAST159;
									_t = __t159;
									_t = _t.getNextSibling();
									}
								}
								catch (RecognitionException pe) {
									synPredMatched161 = false;
								}
								_t = __t161;
inputState.guessing--;
							}
							if ( synPredMatched161 ) {
								AST __t162 = _t;
								ti = _t==ASTNULL ? null :(AST)_t;
								AST ti_AST_in = null;
								ti_AST = astFactory.create(ti);
								ASTPair __currentAST162 = currentAST.copy();
								currentAST.root = currentAST.child;
								currentAST.child = null;
								match(_t,OR_CONSTRAINT_SET);
								_t = _t.getFirstChild();
								p = _t==ASTNULL ? null : (AST)_t;
								n_abstract_constraint_list_notand(_t);
								_t = _retTree;
								p_AST = (AST)returnAST;
								AST __t163 = _t;
								tj = _t==ASTNULL ? null :(AST)_t;
								AST tj_AST_in = null;
								tj_AST = astFactory.create(tj);
								ASTPair __currentAST163 = currentAST.copy();
								currentAST.root = currentAST.child;
								currentAST.child = null;
								match(_t,AND_CONSTRAINT_SET);
								_t = _t.getFirstChild();
								q = _t==ASTNULL ? null : (AST)_t;
								n_abstract_constraint(_t);
								_t = _retTree;
								q_AST = (AST)returnAST;
								r = _t==ASTNULL ? null : (AST)_t;
								n_abstract_constraint_list(_t);
								_t = _retTree;
								r_AST = (AST)returnAST;
								currentAST = __currentAST163;
								_t = __t163;
								_t = _t.getNextSibling();
								currentAST = __currentAST162;
								_t = __t162;
								_t = _t.getNextSibling();
								if ( inputState.guessing==0 ) {
									or_constraint_set_AST = (AST)currentAST.root;
									AST ti2_AST = astFactory.create(ti);
									AST p2_AST = astFactory.dupList(p_AST);
									AST tj2_AST = astFactory.create(tj);
									or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(3)).add(tj_AST).add((AST)astFactory.make( (new ASTArray(3)).add(ti_AST).add(p_AST).add(q_AST))).add((AST)astFactory.make( (new ASTArray(3)).add(ti2_AST).add(p2_AST).add((AST)astFactory.make( (new ASTArray(2)).add(tj2_AST).add(r_AST))))));
									currentAST.root = or_constraint_set_AST;
									currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
										or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
									currentAST.advanceChildToEnd();
								}
							}
							else {
								boolean synPredMatched166 = false;
								if (_t==null) _t=ASTNULL;
								if (((_t.getType()==OR_CONSTRAINT_SET))) {
									AST __t166 = _t;
									synPredMatched166 = true;
									inputState.guessing++;
									try {
										{
										AST __t165 = _t;
										ASTPair __currentAST165 = currentAST.copy();
										currentAST.root = currentAST.child;
										currentAST.child = null;
										match(_t,OR_CONSTRAINT_SET);
										_t = _t.getFirstChild();
										n_abstract_constraint(_t);
										_t = _retTree;
										n_abstract_constraint(_t);
										_t = _retTree;
										currentAST = __currentAST165;
										_t = __t165;
										_t = _t.getNextSibling();
										}
									}
									catch (RecognitionException pe) {
										synPredMatched166 = false;
									}
									_t = __t166;
inputState.guessing--;
								}
								if ( synPredMatched166 ) {
									AST __t167 = _t;
									AST tmp87_AST = null;
									AST tmp87_AST_in = null;
									tmp87_AST = astFactory.create((AST)_t);
									tmp87_AST_in = (AST)_t;
									astFactory.addASTChild(currentAST, tmp87_AST);
									ASTPair __currentAST167 = currentAST.copy();
									currentAST.root = currentAST.child;
									currentAST.child = null;
									match(_t,OR_CONSTRAINT_SET);
									_t = _t.getFirstChild();
									{
									int _cnt169=0;
									_loop169:
									do {
										if (_t==null) _t=ASTNULL;
										if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
											abstract_constraint(_t);
											_t = _retTree;
											astFactory.addASTChild(currentAST, returnAST);
										}
										else {
											if ( _cnt169>=1 ) { break _loop169; } else {throw new NoViableAltException(_t);}
										}
										
										_cnt169++;
									} while (true);
									}
									currentAST = __currentAST167;
									_t = __t167;
									_t = _t.getNextSibling();
									or_constraint_set_AST = (AST)currentAST.root;
								}
								else if ((_t.getType()==OR_CONSTRAINT_SET)) {
									AST __t170 = _t;
									AST tmp88_AST = null;
									AST tmp88_AST_in = null;
									tmp88_AST = astFactory.create((AST)_t);
									tmp88_AST_in = (AST)_t;
									ASTPair __currentAST170 = currentAST.copy();
									currentAST.root = currentAST.child;
									currentAST.child = null;
									match(_t,OR_CONSTRAINT_SET);
									_t = _t.getFirstChild();
									z = _t==ASTNULL ? null : (AST)_t;
									n_abstract_constraint(_t);
									_t = _retTree;
									z_AST = (AST)returnAST;
									currentAST = __currentAST170;
									_t = __t170;
									_t = _t.getNextSibling();
									if ( inputState.guessing==0 ) {
										or_constraint_set_AST = (AST)currentAST.root;
										or_constraint_set_AST = z_AST;
										currentAST.root = or_constraint_set_AST;
										currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
											or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
										currentAST.advanceChildToEnd();
									}
								}
								else {
									throw new NoViableAltException(_t);
								}
								}}}}}}
								returnAST = or_constraint_set_AST;
								_retTree = _t;
							}
							
	public final void subquery_constraint(AST _t) throws RecognitionException {
		
		AST subquery_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST subquery_constraint_AST = null;
		
		AST __t195 = _t;
		AST tmp89_AST = null;
		AST tmp89_AST_in = null;
		tmp89_AST = astFactory.create((AST)_t);
		tmp89_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp89_AST);
		ASTPair __currentAST195 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SUBQUERY_CONSTRAINT);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		sql(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST195;
		_t = __t195;
		_t = _t.getNextSibling();
		subquery_constraint_AST = (AST)currentAST.root;
		returnAST = subquery_constraint_AST;
		_retTree = _t;
	}
	
	public final void inlist_constraint(AST _t) throws RecognitionException {
		
		AST inlist_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST inlist_constraint_AST = null;
		
		AST __t197 = _t;
		AST tmp90_AST = null;
		AST tmp90_AST_in = null;
		tmp90_AST = astFactory.create((AST)_t);
		tmp90_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp90_AST);
		ASTPair __currentAST197 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,INLIST_CONSTRAINT);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		int _cnt199=0;
		_loop199:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==CONSTANT)) {
				constant(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt199>=1 ) { break _loop199; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt199++;
		} while (true);
		}
		currentAST = __currentAST197;
		_t = __t197;
		_t = _t.getNextSibling();
		inlist_constraint_AST = (AST)currentAST.root;
		returnAST = inlist_constraint_AST;
		_retTree = _t;
	}
	
	public final void null_constraint(AST _t) throws RecognitionException {
		
		AST null_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST null_constraint_AST = null;
		AST v_AST = null;
		AST v = null;
		
		boolean synPredMatched108 = false;
		if (_t==null) _t=ASTNULL;
		if (((_t.getType()==NULL_CONSTRAINT))) {
			AST __t108 = _t;
			synPredMatched108 = true;
			inputState.guessing++;
			try {
				{
				AST __t107 = _t;
				ASTPair __currentAST107 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,NULL_CONSTRAINT);
				_t = _t.getFirstChild();
				abstract_value(_t);
				_t = _retTree;
				match(_t,LITERAL_not);
				_t = _t.getNextSibling();
				currentAST = __currentAST107;
				_t = __t107;
				_t = _t.getNextSibling();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched108 = false;
			}
			_t = __t108;
inputState.guessing--;
		}
		if ( synPredMatched108 ) {
			AST __t109 = _t;
			AST tmp91_AST = null;
			AST tmp91_AST_in = null;
			tmp91_AST = astFactory.create((AST)_t);
			tmp91_AST_in = (AST)_t;
			ASTPair __currentAST109 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NULL_CONSTRAINT);
			_t = _t.getFirstChild();
			v = _t==ASTNULL ? null : (AST)_t;
			abstract_value(_t);
			_t = _retTree;
			v_AST = (AST)returnAST;
			AST tmp92_AST_in = null;
			match(_t,LITERAL_not);
			_t = _t.getNextSibling();
			currentAST = __currentAST109;
			_t = __t109;
			_t = _t.getNextSibling();
			if ( inputState.guessing==0 ) {
				null_constraint_AST = (AST)currentAST.root;
				null_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(tmp91_AST)).add(v_AST))));
				currentAST.root = null_constraint_AST;
				currentAST.child = null_constraint_AST!=null &&null_constraint_AST.getFirstChild()!=null ?
					null_constraint_AST.getFirstChild() : null_constraint_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else if ((_t.getType()==NULL_CONSTRAINT)) {
			AST __t110 = _t;
			AST tmp93_AST = null;
			AST tmp93_AST_in = null;
			tmp93_AST = astFactory.create((AST)_t);
			tmp93_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp93_AST);
			ASTPair __currentAST110 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NULL_CONSTRAINT);
			_t = _t.getFirstChild();
			abstract_value(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST110;
			_t = __t110;
			_t = _t.getNextSibling();
			null_constraint_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(_t);
		}
		
		returnAST = null_constraint_AST;
		_retTree = _t;
	}
	
	public final void comparison_op(AST _t) throws RecognitionException {
		
		AST comparison_op_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST comparison_op_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQ:
		{
			AST tmp94_AST = null;
			AST tmp94_AST_in = null;
			tmp94_AST = astFactory.create((AST)_t);
			tmp94_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp94_AST);
			match(_t,EQ);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case LT:
		{
			AST tmp95_AST = null;
			AST tmp95_AST_in = null;
			tmp95_AST = astFactory.create((AST)_t);
			tmp95_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp95_AST);
			match(_t,LT);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case GT:
		{
			AST tmp96_AST = null;
			AST tmp96_AST_in = null;
			tmp96_AST = astFactory.create((AST)_t);
			tmp96_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp96_AST);
			match(_t,GT);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case NOT_EQ:
		{
			AST tmp97_AST = null;
			AST tmp97_AST_in = null;
			tmp97_AST = astFactory.create((AST)_t);
			tmp97_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp97_AST);
			match(_t,NOT_EQ);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case LE:
		{
			AST tmp98_AST = null;
			AST tmp98_AST_in = null;
			tmp98_AST = astFactory.create((AST)_t);
			tmp98_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp98_AST);
			match(_t,LE);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case GE:
		{
			AST tmp99_AST = null;
			AST tmp99_AST_in = null;
			tmp99_AST = astFactory.create((AST)_t);
			tmp99_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp99_AST);
			match(_t,GE);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case GORNULL:
		{
			AST tmp100_AST = null;
			AST tmp100_AST_in = null;
			tmp100_AST = astFactory.create((AST)_t);
			tmp100_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp100_AST);
			match(_t,GORNULL);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_like:
		{
			AST tmp101_AST = null;
			AST tmp101_AST_in = null;
			tmp101_AST = astFactory.create((AST)_t);
			tmp101_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp101_AST);
			match(_t,LITERAL_like);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_not:
		{
			AST tmp102_AST = null;
			AST tmp102_AST_in = null;
			tmp102_AST = astFactory.create((AST)_t);
			tmp102_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp102_AST);
			match(_t,LITERAL_not);
			_t = _t.getNextSibling();
			AST tmp103_AST = null;
			AST tmp103_AST_in = null;
			tmp103_AST = astFactory.create((AST)_t);
			tmp103_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp103_AST);
			match(_t,LITERAL_like);
			_t = _t.getNextSibling();
			comparison_op_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = comparison_op_AST;
		_retTree = _t;
	}
	
	public final void n_abstract_constraint(AST _t) throws RecognitionException {
		
		AST n_abstract_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_abstract_constraint_AST = null;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case CONSTRAINT:
		{
			n_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case NOT_CONSTRAINT:
		{
			n_not_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case AND_CONSTRAINT_SET:
		{
			n_and_constraint_set(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case OR_CONSTRAINT_SET:
		{
			n_or_constraint_set(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case SUBQUERY_CONSTRAINT:
		{
			subquery_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case INLIST_CONSTRAINT:
		{
			inlist_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		case NULL_CONSTRAINT:
		{
			n_null_constraint(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			n_abstract_constraint_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = n_abstract_constraint_AST;
		_retTree = _t;
	}
	
	public final void n_abstract_constraint_list(AST _t) throws RecognitionException {
		
		AST n_abstract_constraint_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_abstract_constraint_list_AST = null;
		
		{
		int _cnt228=0;
		_loop228:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
				n_abstract_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt228>=1 ) { break _loop228; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt228++;
		} while (true);
		}
		n_abstract_constraint_list_AST = (AST)currentAST.root;
		returnAST = n_abstract_constraint_list_AST;
		_retTree = _t;
	}
	
	public final void n_abstract_constraint_list_notor(AST _t) throws RecognitionException {
		
		AST n_abstract_constraint_list_notor_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_abstract_constraint_list_notor_AST = null;
		
		{
		int _cnt234=0;
		_loop234:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CONSTRAINT:
			{
				n_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOT_CONSTRAINT:
			{
				n_not_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case AND_CONSTRAINT_SET:
			{
				n_and_constraint_set(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SUBQUERY_CONSTRAINT:
			{
				subquery_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case INLIST_CONSTRAINT:
			{
				inlist_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NULL_CONSTRAINT:
			{
				n_null_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				if ( _cnt234>=1 ) { break _loop234; } else {throw new NoViableAltException(_t);}
			}
			}
			_cnt234++;
		} while (true);
		}
		n_abstract_constraint_list_notor_AST = (AST)currentAST.root;
		returnAST = n_abstract_constraint_list_notor_AST;
		_retTree = _t;
	}
	
	public final void n_abstract_constraint_list_notand(AST _t) throws RecognitionException {
		
		AST n_abstract_constraint_list_notand_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_abstract_constraint_list_notand_AST = null;
		
		{
		int _cnt231=0;
		_loop231:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CONSTRAINT:
			{
				n_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOT_CONSTRAINT:
			{
				n_not_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case OR_CONSTRAINT_SET:
			{
				n_or_constraint_set(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SUBQUERY_CONSTRAINT:
			{
				subquery_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case INLIST_CONSTRAINT:
			{
				inlist_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NULL_CONSTRAINT:
			{
				n_null_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				if ( _cnt231>=1 ) { break _loop231; } else {throw new NoViableAltException(_t);}
			}
			}
			_cnt231++;
		} while (true);
		}
		n_abstract_constraint_list_notand_AST = (AST)currentAST.root;
		returnAST = n_abstract_constraint_list_notand_AST;
		_retTree = _t;
	}
	
	public final void abstract_constraint_list(AST _t) throws RecognitionException {
		
		AST abstract_constraint_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abstract_constraint_list_AST = null;
		
		{
		int _cnt203=0;
		_loop203:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
				abstract_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt203>=1 ) { break _loop203; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt203++;
		} while (true);
		}
		abstract_constraint_list_AST = (AST)currentAST.root;
		returnAST = abstract_constraint_list_AST;
		_retTree = _t;
	}
	
	public final void abstract_constraint_list_notand(AST _t) throws RecognitionException {
		
		AST abstract_constraint_list_notand_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abstract_constraint_list_notand_AST = null;
		
		{
		int _cnt206=0;
		_loop206:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CONSTRAINT:
			{
				constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOT_CONSTRAINT:
			{
				not_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case OR_CONSTRAINT_SET:
			{
				or_constraint_set(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SUBQUERY_CONSTRAINT:
			{
				subquery_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case INLIST_CONSTRAINT:
			{
				inlist_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				if ( _cnt206>=1 ) { break _loop206; } else {throw new NoViableAltException(_t);}
			}
			}
			_cnt206++;
		} while (true);
		}
		abstract_constraint_list_notand_AST = (AST)currentAST.root;
		returnAST = abstract_constraint_list_notand_AST;
		_retTree = _t;
	}
	
	public final void abstract_constraint_list_notor(AST _t) throws RecognitionException {
		
		AST abstract_constraint_list_notor_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST abstract_constraint_list_notor_AST = null;
		
		{
		int _cnt209=0;
		_loop209:
		do {
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CONSTRAINT:
			{
				constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case NOT_CONSTRAINT:
			{
				not_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case AND_CONSTRAINT_SET:
			{
				and_constraint_set(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SUBQUERY_CONSTRAINT:
			{
				subquery_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case INLIST_CONSTRAINT:
			{
				inlist_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				if ( _cnt209>=1 ) { break _loop209; } else {throw new NoViableAltException(_t);}
			}
			}
			_cnt209++;
		} while (true);
		}
		abstract_constraint_list_notor_AST = (AST)currentAST.root;
		returnAST = abstract_constraint_list_notor_AST;
		_retTree = _t;
	}
	
	public final void n_constraint(AST _t) throws RecognitionException {
		
		AST n_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_constraint_AST = null;
		
		AST __t212 = _t;
		AST tmp104_AST = null;
		AST tmp104_AST_in = null;
		tmp104_AST = astFactory.create((AST)_t);
		tmp104_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp104_AST);
		ASTPair __currentAST212 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,CONSTRAINT);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		comparison_op(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST212;
		_t = __t212;
		_t = _t.getNextSibling();
		n_constraint_AST = (AST)currentAST.root;
		returnAST = n_constraint_AST;
		_retTree = _t;
	}
	
	public final void n_not_constraint(AST _t) throws RecognitionException {
		
		AST n_not_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_not_constraint_AST = null;
		
		AST __t217 = _t;
		AST tmp105_AST = null;
		AST tmp105_AST_in = null;
		tmp105_AST = astFactory.create((AST)_t);
		tmp105_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp105_AST);
		ASTPair __currentAST217 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,NOT_CONSTRAINT);
		_t = _t.getFirstChild();
		n_abstract_constraint(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		currentAST = __currentAST217;
		_t = __t217;
		_t = _t.getNextSibling();
		n_not_constraint_AST = (AST)currentAST.root;
		returnAST = n_not_constraint_AST;
		_retTree = _t;
	}
	
	public final void n_and_constraint_set(AST _t) throws RecognitionException {
		
		AST n_and_constraint_set_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_and_constraint_set_AST = null;
		
		AST __t219 = _t;
		AST tmp106_AST = null;
		AST tmp106_AST_in = null;
		tmp106_AST = astFactory.create((AST)_t);
		tmp106_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp106_AST);
		ASTPair __currentAST219 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,AND_CONSTRAINT_SET);
		_t = _t.getFirstChild();
		{
		int _cnt221=0;
		_loop221:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
				n_abstract_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt221>=1 ) { break _loop221; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt221++;
		} while (true);
		}
		currentAST = __currentAST219;
		_t = __t219;
		_t = _t.getNextSibling();
		n_and_constraint_set_AST = (AST)currentAST.root;
		returnAST = n_and_constraint_set_AST;
		_retTree = _t;
	}
	
	public final void n_or_constraint_set(AST _t) throws RecognitionException {
		
		AST n_or_constraint_set_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_or_constraint_set_AST = null;
		
		AST __t223 = _t;
		AST tmp107_AST = null;
		AST tmp107_AST_in = null;
		tmp107_AST = astFactory.create((AST)_t);
		tmp107_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp107_AST);
		ASTPair __currentAST223 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,OR_CONSTRAINT_SET);
		_t = _t.getFirstChild();
		{
		int _cnt225=0;
		_loop225:
		do {
			if (_t==null) _t=ASTNULL;
			if (((_t.getType() >= CONSTRAINT && _t.getType() <= INLIST_CONSTRAINT))) {
				n_abstract_constraint(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt225>=1 ) { break _loop225; } else {throw new NoViableAltException(_t);}
			}
			
			_cnt225++;
		} while (true);
		}
		currentAST = __currentAST223;
		_t = __t223;
		_t = _t.getNextSibling();
		n_or_constraint_set_AST = (AST)currentAST.root;
		returnAST = n_or_constraint_set_AST;
		_retTree = _t;
	}
	
	public final void n_null_constraint(AST _t) throws RecognitionException {
		
		AST n_null_constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST n_null_constraint_AST = null;
		
		AST __t214 = _t;
		AST tmp108_AST = null;
		AST tmp108_AST_in = null;
		tmp108_AST = astFactory.create((AST)_t);
		tmp108_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp108_AST);
		ASTPair __currentAST214 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,NULL_CONSTRAINT);
		_t = _t.getFirstChild();
		abstract_value(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		{
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LITERAL_not:
		{
			AST tmp109_AST = null;
			AST tmp109_AST_in = null;
			tmp109_AST = astFactory.create((AST)_t);
			tmp109_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp109_AST);
			match(_t,LITERAL_not);
			_t = _t.getNextSibling();
			break;
		}
		case 3:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		}
		currentAST = __currentAST214;
		_t = __t214;
		_t = _t.getNextSibling();
		n_null_constraint_AST = (AST)currentAST.root;
		returnAST = n_null_constraint_AST;
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"SQL_STATEMENT",
		"SELECT_LIST",
		"FROM_LIST",
		"WHERE_CLAUSE",
		"GROUP_CLAUSE",
		"HAVING_CLAUSE",
		"ORDER_CLAUSE",
		"LIMIT_CLAUSE",
		"SELECT_VALUE",
		"TABLE_ALIAS",
		"FIELD_ALIAS",
		"TABLE",
		"TABLE_NAME",
		"SUBQUERY",
		"CONSTANT",
		"FIELD",
		"FIELD_NAME",
		"SAFE_FUNCTION",
		"UNSAFE_FUNCTION",
		"CONSTRAINT",
		"NULL_CONSTRAINT",
		"NOT_CONSTRAINT",
		"AND_CONSTRAINT_SET",
		"OR_CONSTRAINT_SET",
		"SUBQUERY_CONSTRAINT",
		"INLIST_CONSTRAINT",
		"ORDER_DESC",
		"BIOSEG_CONSTRAINT",
		"\"explain\"",
		"\"distinct\"",
		"INTEGER",
		"ALIAS",
		"IDENTIFIER",
		"QUOTED_STRING",
		"ESCAPED_STRING",
		"FLOAT",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"TYPECAST",
		"\"boolean\"",
		"\"real\"",
		"\"double\"",
		"\"smallint\"",
		"\"integer\"",
		"\"bigint\"",
		"\"numeric\"",
		"\"text\"",
		"\"count\"",
		"\"max\"",
		"\"min\"",
		"\"sum\"",
		"\"avg\"",
		"\"lower\"",
		"\"upper\"",
		"\"strpos\"",
		"\"substr\"",
		"\"coalesce\"",
		"\"stddev\"",
		"PLUS",
		"PERCENT",
		"ASTERISK",
		"DIVIDE",
		"POWER",
		"MINUS",
		"NOT_EQ",
		"GE",
		"LE",
		"GT",
		"\"not\"",
		"\"like\"",
		"EQ",
		"LT",
		"GORNULL",
		"SEMI",
		"\"union\"",
		"\"select\"",
		"\"all\"",
		"COMMA",
		"\"from\"",
		"\"where\"",
		"\"group\"",
		"\"by\"",
		"\"having\"",
		"\"order\"",
		"\"limit\"",
		"\"offset\"",
		"\"as\"",
		"\"desc\"",
		"OPEN_PAREN",
		"CLOSE_PAREN",
		"DOT",
		"COLONTYPE",
		"\"precision\"",
		"\"or\"",
		"\"is\"",
		"\"bioseg_create\"",
		"OVERLAP",
		"INSIDE",
		"SURROUND",
		"\"and\"",
		"\"in\"",
		"AT_SIGN",
		"VERTBAR",
		"WS"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 8796100100096L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 8797173841920L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	}
	
