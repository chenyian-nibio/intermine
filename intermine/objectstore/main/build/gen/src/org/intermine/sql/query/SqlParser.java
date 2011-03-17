// $ANTLR 2.7.6 (20060327): "intermine_sql.g" -> "SqlParser.java"$

package org.intermine.sql.query;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import java.util.IdentityHashMap;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class SqlParser extends antlr.LLkParser       implements SqlTokenTypes
 {

protected SqlParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public SqlParser(TokenBuffer tokenBuf) {
  this(tokenBuf,6);
}

protected SqlParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public SqlParser(TokenStream lexer) {
  this(lexer,6);
}

public SqlParser(ParserSharedInputState state) {
  super(state,6);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	IdentityHashMap start_ruleCache = new IdentityHashMap();
	public final void start_rule() throws RecognitionException, TokenStreamException {
		Object retval = start_ruleCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST start_rule_AST = null;
				
				sql();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case SEMI:
				{
					match(SEMI);
					break;
				}
				case EOF:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				start_rule_AST = (AST)currentAST.root;
				returnAST = start_rule_AST;
				start_ruleCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				start_ruleCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				start_ruleCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap sqlCache = new IdentityHashMap();
	public final void sql() throws RecognitionException, TokenStreamException {
		Object retval = sqlCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST sql_AST = null;
				
				sql_statement();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop239:
				do {
					if ((LA(1)==LITERAL_union)) {
						match(LITERAL_union);
						sql_statement();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop239;
					}
					
				} while (true);
				}
				sql_AST = (AST)currentAST.root;
				returnAST = sql_AST;
				sqlCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				sqlCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				sqlCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap sql_statementCache = new IdentityHashMap();
	public final void sql_statement() throws RecognitionException, TokenStreamException {
		Object retval = sql_statementCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST sql_statement_AST = null;
				
				select_command();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					sql_statement_AST = (AST)currentAST.root;
					sql_statement_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SQL_STATEMENT,"SQL_STATEMENT")).add(sql_statement_AST));
					currentAST.root = sql_statement_AST;
					currentAST.child = sql_statement_AST!=null &&sql_statement_AST.getFirstChild()!=null ?
						sql_statement_AST.getFirstChild() : sql_statement_AST;
					currentAST.advanceChildToEnd();
				}
				sql_statement_AST = (AST)currentAST.root;
				returnAST = sql_statement_AST;
				sql_statementCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				sql_statementCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				sql_statementCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap select_commandCache = new IdentityHashMap();
	public final void select_command() throws RecognitionException, TokenStreamException {
		Object retval = select_commandCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST select_command_AST = null;
				
				{
				switch ( LA(1)) {
				case LITERAL_explain:
				{
					AST tmp112_AST = null;
					tmp112_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp112_AST);
					match(LITERAL_explain);
					break;
				}
				case LITERAL_select:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(LITERAL_select);
				{
				switch ( LA(1)) {
				case LITERAL_all:
				{
					match(LITERAL_all);
					break;
				}
				case LITERAL_distinct:
				{
					AST tmp115_AST = null;
					tmp115_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp115_AST);
					match(LITERAL_distinct);
					break;
				}
				case INTEGER:
				case IDENTIFIER:
				case QUOTED_STRING:
				case ESCAPED_STRING:
				case FLOAT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				case LITERAL_count:
				case LITERAL_max:
				case LITERAL_min:
				case LITERAL_sum:
				case LITERAL_avg:
				case LITERAL_lower:
				case LITERAL_upper:
				case LITERAL_strpos:
				case LITERAL_substr:
				case LITERAL_coalesce:
				case LITERAL_stddev:
				case OPEN_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				select_list();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case LITERAL_from:
				{
					from_list();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case LITERAL_where:
					{
						where_clause();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case SEMI:
					case LITERAL_union:
					case LITERAL_group:
					case LITERAL_order:
					case LITERAL_limit:
					case CLOSE_PAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					switch ( LA(1)) {
					case LITERAL_group:
					{
						group_clause();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case LITERAL_having:
						{
							having_clause();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case EOF:
						case SEMI:
						case LITERAL_union:
						case LITERAL_order:
						case LITERAL_limit:
						case CLOSE_PAREN:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						break;
					}
					case EOF:
					case SEMI:
					case LITERAL_union:
					case LITERAL_order:
					case LITERAL_limit:
					case CLOSE_PAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					switch ( LA(1)) {
					case LITERAL_order:
					{
						order_clause();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case SEMI:
					case LITERAL_union:
					case LITERAL_limit:
					case CLOSE_PAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					switch ( LA(1)) {
					case LITERAL_limit:
					{
						limit_clause();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case SEMI:
					case LITERAL_union:
					case CLOSE_PAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case EOF:
				case SEMI:
				case LITERAL_union:
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				select_command_AST = (AST)currentAST.root;
				returnAST = select_command_AST;
				select_commandCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				select_commandCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				select_commandCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap select_listCache = new IdentityHashMap();
	public final void select_list() throws RecognitionException, TokenStreamException {
		Object retval = select_listCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST select_list_AST = null;
				
				select_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop252:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						select_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop252;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					select_list_AST = (AST)currentAST.root;
					select_list_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SELECT_LIST,"SELECT_LIST")).add(select_list_AST));
					currentAST.root = select_list_AST;
					currentAST.child = select_list_AST!=null &&select_list_AST.getFirstChild()!=null ?
						select_list_AST.getFirstChild() : select_list_AST;
					currentAST.advanceChildToEnd();
				}
				select_list_AST = (AST)currentAST.root;
				returnAST = select_list_AST;
				select_listCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				select_listCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				select_listCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap from_listCache = new IdentityHashMap();
	public final void from_list() throws RecognitionException, TokenStreamException {
		Object retval = from_listCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST from_list_AST = null;
				
				match(LITERAL_from);
				abstract_table();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop255:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						abstract_table();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop255;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					from_list_AST = (AST)currentAST.root;
					from_list_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FROM_LIST,"FROM_LIST")).add(from_list_AST));
					currentAST.root = from_list_AST;
					currentAST.child = from_list_AST!=null &&from_list_AST.getFirstChild()!=null ?
						from_list_AST.getFirstChild() : from_list_AST;
					currentAST.advanceChildToEnd();
				}
				from_list_AST = (AST)currentAST.root;
				returnAST = from_list_AST;
				from_listCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				from_listCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				from_listCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap where_clauseCache = new IdentityHashMap();
	public final void where_clause() throws RecognitionException, TokenStreamException {
		Object retval = where_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST where_clause_AST = null;
				
				match(LITERAL_where);
				abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					where_clause_AST = (AST)currentAST.root;
					where_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(WHERE_CLAUSE,"WHERE_CLAUSE")).add(where_clause_AST));
					currentAST.root = where_clause_AST;
					currentAST.child = where_clause_AST!=null &&where_clause_AST.getFirstChild()!=null ?
						where_clause_AST.getFirstChild() : where_clause_AST;
					currentAST.advanceChildToEnd();
				}
				where_clause_AST = (AST)currentAST.root;
				returnAST = where_clause_AST;
				where_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				where_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				where_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap group_clauseCache = new IdentityHashMap();
	public final void group_clause() throws RecognitionException, TokenStreamException {
		Object retval = group_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST group_clause_AST = null;
				
				match(LITERAL_group);
				match(LITERAL_by);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop259:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop259;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					group_clause_AST = (AST)currentAST.root;
					group_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(GROUP_CLAUSE,"GROUP_CLAUSE")).add(group_clause_AST));
					currentAST.root = group_clause_AST;
					currentAST.child = group_clause_AST!=null &&group_clause_AST.getFirstChild()!=null ?
						group_clause_AST.getFirstChild() : group_clause_AST;
					currentAST.advanceChildToEnd();
				}
				group_clause_AST = (AST)currentAST.root;
				returnAST = group_clause_AST;
				group_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				group_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				group_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap having_clauseCache = new IdentityHashMap();
	public final void having_clause() throws RecognitionException, TokenStreamException {
		Object retval = having_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST having_clause_AST = null;
				
				match(LITERAL_having);
				abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					having_clause_AST = (AST)currentAST.root;
					having_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(HAVING_CLAUSE,"HAVING_CLAUSE")).add(having_clause_AST));
					currentAST.root = having_clause_AST;
					currentAST.child = having_clause_AST!=null &&having_clause_AST.getFirstChild()!=null ?
						having_clause_AST.getFirstChild() : having_clause_AST;
					currentAST.advanceChildToEnd();
				}
				having_clause_AST = (AST)currentAST.root;
				returnAST = having_clause_AST;
				having_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				having_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				having_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap order_clauseCache = new IdentityHashMap();
	public final void order_clause() throws RecognitionException, TokenStreamException {
		Object retval = order_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST order_clause_AST = null;
				
				match(LITERAL_order);
				match(LITERAL_by);
				orderby_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop263:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						orderby_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop263;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					order_clause_AST = (AST)currentAST.root;
					order_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ORDER_CLAUSE,"ORDER_CLAUSE")).add(order_clause_AST));
					currentAST.root = order_clause_AST;
					currentAST.child = order_clause_AST!=null &&order_clause_AST.getFirstChild()!=null ?
						order_clause_AST.getFirstChild() : order_clause_AST;
					currentAST.advanceChildToEnd();
				}
				order_clause_AST = (AST)currentAST.root;
				returnAST = order_clause_AST;
				order_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				order_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				order_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap limit_clauseCache = new IdentityHashMap();
	public final void limit_clause() throws RecognitionException, TokenStreamException {
		Object retval = limit_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST limit_clause_AST = null;
				
				match(LITERAL_limit);
				AST tmp128_AST = null;
				tmp128_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp128_AST);
				match(INTEGER);
				{
				switch ( LA(1)) {
				case LITERAL_offset:
				{
					match(LITERAL_offset);
					AST tmp130_AST = null;
					tmp130_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp130_AST);
					match(INTEGER);
					break;
				}
				case EOF:
				case SEMI:
				case LITERAL_union:
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					limit_clause_AST = (AST)currentAST.root;
					limit_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(LIMIT_CLAUSE,"LIMIT_CLAUSE")).add(limit_clause_AST));
					currentAST.root = limit_clause_AST;
					currentAST.child = limit_clause_AST!=null &&limit_clause_AST.getFirstChild()!=null ?
						limit_clause_AST.getFirstChild() : limit_clause_AST;
					currentAST.advanceChildToEnd();
				}
				limit_clause_AST = (AST)currentAST.root;
				returnAST = limit_clause_AST;
				limit_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				limit_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				limit_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap select_valueCache = new IdentityHashMap();
	public final void select_value() throws RecognitionException, TokenStreamException {
		Object retval = select_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST select_value_AST = null;
				
				{
				boolean synPredMatched269 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_3.member(LA(4))) && (_tokenSet_4.member(LA(5))) && (_tokenSet_5.member(LA(6))))) {
					int _m269 = mark();
					synPredMatched269 = true;
					inputState.guessing++;
					try {
						{
						unsafe_function();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched269 = false;
					}
					rewind(_m269);
inputState.guessing--;
				}
				if ( synPredMatched269 ) {
					unsafe_function();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_as);
					field_alias();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					boolean synPredMatched271 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_6.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_3.member(LA(4))) && (_tokenSet_7.member(LA(5))) && (_tokenSet_8.member(LA(6))))) {
						int _m271 = mark();
						synPredMatched271 = true;
						inputState.guessing++;
						try {
							{
							typecast();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched271 = false;
						}
						rewind(_m271);
inputState.guessing--;
					}
					if ( synPredMatched271 ) {
						typecast();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_as);
						field_alias();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else if ((LA(1)==IDENTIFIER) && (_tokenSet_9.member(LA(2))) && (_tokenSet_10.member(LA(3))) && (_tokenSet_11.member(LA(4))) && (_tokenSet_12.member(LA(5))) && (_tokenSet_13.member(LA(6)))) {
						field();
						astFactory.addASTChild(currentAST, returnAST);
						{
						switch ( LA(1)) {
						case LITERAL_as:
						{
							match(LITERAL_as);
							field_alias();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case EOF:
						case SEMI:
						case LITERAL_union:
						case COMMA:
						case LITERAL_from:
						case CLOSE_PAREN:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
					}
					else if ((_tokenSet_14.member(LA(1))) && (LA(2)==LITERAL_as)) {
						constant();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_as);
						field_alias();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else if (((LA(1) >= LITERAL_count && LA(1) <= LITERAL_stddev)) && (LA(2)==OPEN_PAREN) && (_tokenSet_15.member(LA(3))) && (_tokenSet_16.member(LA(4))) && (_tokenSet_17.member(LA(5))) && (_tokenSet_4.member(LA(6)))) {
						safe_function();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_as);
						field_alias();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_18.member(LA(3))) && (_tokenSet_17.member(LA(4))) && (_tokenSet_4.member(LA(5))) && (_tokenSet_5.member(LA(6)))) {
						paren_value();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_as);
						field_alias();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						select_value_AST = (AST)currentAST.root;
						select_value_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SELECT_VALUE,"SELECT_VALUE")).add(select_value_AST));
						currentAST.root = select_value_AST;
						currentAST.child = select_value_AST!=null &&select_value_AST.getFirstChild()!=null ?
							select_value_AST.getFirstChild() : select_value_AST;
						currentAST.advanceChildToEnd();
					}
					select_value_AST = (AST)currentAST.root;
					returnAST = select_value_AST;
					select_valueCache.put(LT(1), returnAST);
				} catch (RecognitionException e) {
					select_valueCache.put(LT(1), e);
					throw e;
				} catch (TokenStreamException e) {
					select_valueCache.put(LT(1), e);
					throw e;
				}
			}
		}
		
	IdentityHashMap abstract_tableCache = new IdentityHashMap();
	public final void abstract_table() throws RecognitionException, TokenStreamException {
		Object retval = abstract_tableCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST abstract_table_AST = null;
				
				switch ( LA(1)) {
				case IDENTIFIER:
				{
					table();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_table_AST = (AST)currentAST.root;
					break;
				}
				case OPEN_PAREN:
				{
					subquery();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_table_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = abstract_table_AST;
				abstract_tableCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				abstract_tableCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				abstract_tableCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap abstract_constraintCache = new IdentityHashMap();
	public final void abstract_constraint() throws RecognitionException, TokenStreamException {
		Object retval = abstract_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST abstract_constraint_AST = null;
				
				boolean synPredMatched328 = false;
				if (((LA(1)==IDENTIFIER) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==GT) && (_tokenSet_14.member(LA(5))) && (LA(6)==LITERAL_or))) {
					int _m328 = mark();
					synPredMatched328 = true;
					inputState.guessing++;
					try {
						{
						gornull_constraint();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched328 = false;
					}
					rewind(_m328);
inputState.guessing--;
				}
				if ( synPredMatched328 ) {
					gornull_constraint();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_constraint_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched330 = false;
					if (((_tokenSet_19.member(LA(1))) && (_tokenSet_20.member(LA(2))) && (_tokenSet_21.member(LA(3))) && (_tokenSet_22.member(LA(4))) && (_tokenSet_23.member(LA(5))) && (_tokenSet_24.member(LA(6))))) {
						int _m330 = mark();
						synPredMatched330 = true;
						inputState.guessing++;
						try {
							{
							constraint_set();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched330 = false;
						}
						rewind(_m330);
inputState.guessing--;
					}
					if ( synPredMatched330 ) {
						constraint_set();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_constraint_AST = (AST)currentAST.root;
					}
					else if ((_tokenSet_19.member(LA(1))) && (_tokenSet_20.member(LA(2))) && (_tokenSet_21.member(LA(3))) && (_tokenSet_25.member(LA(4))) && (_tokenSet_26.member(LA(5))) && (_tokenSet_27.member(LA(6)))) {
						safe_abstract_constraint();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_constraint_AST = (AST)currentAST.root;
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					returnAST = abstract_constraint_AST;
					abstract_constraintCache.put(LT(1), returnAST);
				} catch (RecognitionException e) {
					abstract_constraintCache.put(LT(1), e);
					throw e;
				} catch (TokenStreamException e) {
					abstract_constraintCache.put(LT(1), e);
					throw e;
				}
			}
		}
		
	IdentityHashMap abstract_valueCache = new IdentityHashMap();
	public final void abstract_value() throws RecognitionException, TokenStreamException {
		Object retval = abstract_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST abstract_value_AST = null;
				
				boolean synPredMatched279 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_28.member(LA(4))) && (_tokenSet_29.member(LA(5))) && (_tokenSet_30.member(LA(6))))) {
					int _m279 = mark();
					synPredMatched279 = true;
					inputState.guessing++;
					try {
						{
						unsafe_function();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched279 = false;
					}
					rewind(_m279);
inputState.guessing--;
				}
				if ( synPredMatched279 ) {
					unsafe_function();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_value_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched281 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_6.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_28.member(LA(4))) && (_tokenSet_29.member(LA(5))) && (_tokenSet_30.member(LA(6))))) {
						int _m281 = mark();
						synPredMatched281 = true;
						inputState.guessing++;
						try {
							{
							typecast();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched281 = false;
						}
						rewind(_m281);
inputState.guessing--;
					}
					if ( synPredMatched281 ) {
						typecast();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((_tokenSet_14.member(LA(1))) && (_tokenSet_31.member(LA(2)))) {
						constant();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((LA(1)==IDENTIFIER) && (_tokenSet_32.member(LA(2))) && (_tokenSet_33.member(LA(3))) && (_tokenSet_34.member(LA(4))) && (_tokenSet_35.member(LA(5))) && (_tokenSet_35.member(LA(6)))) {
						field();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if (((LA(1) >= LITERAL_count && LA(1) <= LITERAL_stddev)) && (LA(2)==OPEN_PAREN) && (_tokenSet_15.member(LA(3))) && (_tokenSet_16.member(LA(4))) && (_tokenSet_36.member(LA(5))) && (_tokenSet_29.member(LA(6)))) {
						safe_function();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_18.member(LA(3))) && (_tokenSet_36.member(LA(4))) && (_tokenSet_29.member(LA(5))) && (_tokenSet_30.member(LA(6)))) {
						paren_value();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					returnAST = abstract_value_AST;
					abstract_valueCache.put(LT(1), returnAST);
				} catch (RecognitionException e) {
					abstract_valueCache.put(LT(1), e);
					throw e;
				} catch (TokenStreamException e) {
					abstract_valueCache.put(LT(1), e);
					throw e;
				}
			}
		}
		
	IdentityHashMap orderby_valueCache = new IdentityHashMap();
	public final void orderby_value() throws RecognitionException, TokenStreamException {
		Object retval = orderby_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST orderby_value_AST = null;
				
				boolean synPredMatched276 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_37.member(LA(2))) && (_tokenSet_38.member(LA(3))) && (_tokenSet_39.member(LA(4))) && (_tokenSet_40.member(LA(5))) && (_tokenSet_41.member(LA(6))))) {
					int _m276 = mark();
					synPredMatched276 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(LITERAL_desc);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched276 = false;
					}
					rewind(_m276);
inputState.guessing--;
				}
				if ( synPredMatched276 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_desc);
					if ( inputState.guessing==0 ) {
						orderby_value_AST = (AST)currentAST.root;
						orderby_value_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ORDER_DESC,"ORDER_DESC")).add(orderby_value_AST));
						currentAST.root = orderby_value_AST;
						currentAST.child = orderby_value_AST!=null &&orderby_value_AST.getFirstChild()!=null ?
							orderby_value_AST.getFirstChild() : orderby_value_AST;
						currentAST.advanceChildToEnd();
					}
					orderby_value_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_42.member(LA(2))) && (_tokenSet_43.member(LA(3))) && (_tokenSet_40.member(LA(4))) && (_tokenSet_27.member(LA(5))) && (_tokenSet_13.member(LA(6)))) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					orderby_value_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = orderby_value_AST;
				orderby_valueCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				orderby_valueCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				orderby_valueCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap unsafe_functionCache = new IdentityHashMap();
	public final void unsafe_function() throws RecognitionException, TokenStreamException {
		Object retval = unsafe_functionCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST unsafe_function_AST = null;
				
				{
				boolean synPredMatched310 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_44.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
					int _m310 = mark();
					synPredMatched310 = true;
					inputState.guessing++;
					try {
						{
						safe_abstract_value();
						match(PLUS);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched310 = false;
					}
					rewind(_m310);
inputState.guessing--;
				}
				if ( synPredMatched310 ) {
					safe_abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					{
					int _cnt312=0;
					_loop312:
					do {
						if ((LA(1)==PLUS)) {
							AST tmp138_AST = null;
							tmp138_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp138_AST);
							match(PLUS);
							safe_abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							if ( _cnt312>=1 ) { break _loop312; } else {throw new NoViableAltException(LT(1), getFilename());}
						}
						
						_cnt312++;
					} while (true);
					}
				}
				else {
					boolean synPredMatched314 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_47.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
						int _m314 = mark();
						synPredMatched314 = true;
						inputState.guessing++;
						try {
							{
							safe_abstract_value();
							match(PERCENT);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched314 = false;
						}
						rewind(_m314);
inputState.guessing--;
					}
					if ( synPredMatched314 ) {
						safe_abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
						AST tmp139_AST = null;
						tmp139_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp139_AST);
						match(PERCENT);
						safe_abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						boolean synPredMatched316 = false;
						if (((_tokenSet_0.member(LA(1))) && (_tokenSet_48.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
							int _m316 = mark();
							synPredMatched316 = true;
							inputState.guessing++;
							try {
								{
								safe_abstract_value();
								match(ASTERISK);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched316 = false;
							}
							rewind(_m316);
inputState.guessing--;
						}
						if ( synPredMatched316 ) {
							safe_abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
							{
							int _cnt318=0;
							_loop318:
							do {
								if ((LA(1)==ASTERISK)) {
									AST tmp140_AST = null;
									tmp140_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp140_AST);
									match(ASTERISK);
									safe_abstract_value();
									astFactory.addASTChild(currentAST, returnAST);
								}
								else {
									if ( _cnt318>=1 ) { break _loop318; } else {throw new NoViableAltException(LT(1), getFilename());}
								}
								
								_cnt318++;
							} while (true);
							}
						}
						else {
							boolean synPredMatched320 = false;
							if (((_tokenSet_0.member(LA(1))) && (_tokenSet_49.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
								int _m320 = mark();
								synPredMatched320 = true;
								inputState.guessing++;
								try {
									{
									safe_abstract_value();
									match(DIVIDE);
									}
								}
								catch (RecognitionException pe) {
									synPredMatched320 = false;
								}
								rewind(_m320);
inputState.guessing--;
							}
							if ( synPredMatched320 ) {
								safe_abstract_value();
								astFactory.addASTChild(currentAST, returnAST);
								AST tmp141_AST = null;
								tmp141_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp141_AST);
								match(DIVIDE);
								safe_abstract_value();
								astFactory.addASTChild(currentAST, returnAST);
							}
							else {
								boolean synPredMatched322 = false;
								if (((_tokenSet_0.member(LA(1))) && (_tokenSet_50.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
									int _m322 = mark();
									synPredMatched322 = true;
									inputState.guessing++;
									try {
										{
										safe_abstract_value();
										match(POWER);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched322 = false;
									}
									rewind(_m322);
inputState.guessing--;
								}
								if ( synPredMatched322 ) {
									safe_abstract_value();
									astFactory.addASTChild(currentAST, returnAST);
									AST tmp142_AST = null;
									tmp142_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp142_AST);
									match(POWER);
									safe_abstract_value();
									astFactory.addASTChild(currentAST, returnAST);
								}
								else {
									boolean synPredMatched324 = false;
									if (((_tokenSet_0.member(LA(1))) && (_tokenSet_51.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
										int _m324 = mark();
										synPredMatched324 = true;
										inputState.guessing++;
										try {
											{
											safe_abstract_value();
											match(MINUS);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched324 = false;
										}
										rewind(_m324);
inputState.guessing--;
									}
									if ( synPredMatched324 ) {
										safe_abstract_value();
										astFactory.addASTChild(currentAST, returnAST);
										AST tmp143_AST = null;
										tmp143_AST = astFactory.create(LT(1));
										astFactory.addASTChild(currentAST, tmp143_AST);
										match(MINUS);
										safe_abstract_value();
										astFactory.addASTChild(currentAST, returnAST);
									}
									else {
										throw new NoViableAltException(LT(1), getFilename());
									}
									}}}}}
									}
									if ( inputState.guessing==0 ) {
										unsafe_function_AST = (AST)currentAST.root;
										unsafe_function_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(UNSAFE_FUNCTION,"UNSAFE_FUNCTION")).add(unsafe_function_AST));
										currentAST.root = unsafe_function_AST;
										currentAST.child = unsafe_function_AST!=null &&unsafe_function_AST.getFirstChild()!=null ?
											unsafe_function_AST.getFirstChild() : unsafe_function_AST;
										currentAST.advanceChildToEnd();
									}
									unsafe_function_AST = (AST)currentAST.root;
									returnAST = unsafe_function_AST;
									unsafe_functionCache.put(LT(1), returnAST);
								} catch (RecognitionException e) {
									unsafe_functionCache.put(LT(1), e);
									throw e;
								} catch (TokenStreamException e) {
									unsafe_functionCache.put(LT(1), e);
									throw e;
								}
							}
						}
						
	IdentityHashMap field_aliasCache = new IdentityHashMap();
	public final void field_alias() throws RecognitionException, TokenStreamException {
		Object retval = field_aliasCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST field_alias_AST = null;
				
				{
				switch ( LA(1)) {
				case ALIAS:
				{
					AST tmp144_AST = null;
					tmp144_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp144_AST);
					match(ALIAS);
					break;
				}
				case IDENTIFIER:
				{
					AST tmp145_AST = null;
					tmp145_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp145_AST);
					match(IDENTIFIER);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					field_alias_AST = (AST)currentAST.root;
					field_alias_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD_ALIAS,"FIELD_ALIAS")).add(field_alias_AST));
					currentAST.root = field_alias_AST;
					currentAST.child = field_alias_AST!=null &&field_alias_AST.getFirstChild()!=null ?
						field_alias_AST.getFirstChild() : field_alias_AST;
					currentAST.advanceChildToEnd();
				}
				field_alias_AST = (AST)currentAST.root;
				returnAST = field_alias_AST;
				field_aliasCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				field_aliasCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				field_aliasCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap typecastCache = new IdentityHashMap();
	public final void typecast() throws RecognitionException, TokenStreamException {
		Object retval = typecastCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST typecast_AST = null;
				
				{
				switch ( LA(1)) {
				case INTEGER:
				case QUOTED_STRING:
				case ESCAPED_STRING:
				case FLOAT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				{
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case IDENTIFIER:
				{
					field();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LITERAL_count:
				case LITERAL_max:
				case LITERAL_min:
				case LITERAL_sum:
				case LITERAL_avg:
				case LITERAL_lower:
				case LITERAL_upper:
				case LITERAL_strpos:
				case LITERAL_substr:
				case LITERAL_coalesce:
				case LITERAL_stddev:
				{
					safe_function();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case OPEN_PAREN:
				{
					paren_value();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				int _cnt303=0;
				_loop303:
				do {
					if ((LA(1)==COLONTYPE)) {
						match(COLONTYPE);
						{
						switch ( LA(1)) {
						case LITERAL_boolean:
						{
							AST tmp147_AST = null;
							tmp147_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp147_AST);
							match(LITERAL_boolean);
							break;
						}
						case LITERAL_real:
						{
							AST tmp148_AST = null;
							tmp148_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp148_AST);
							match(LITERAL_real);
							break;
						}
						case LITERAL_double:
						{
							AST tmp149_AST = null;
							tmp149_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp149_AST);
							match(LITERAL_double);
							match(LITERAL_precision);
							break;
						}
						case LITERAL_smallint:
						{
							AST tmp151_AST = null;
							tmp151_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp151_AST);
							match(LITERAL_smallint);
							break;
						}
						case LITERAL_integer:
						{
							AST tmp152_AST = null;
							tmp152_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp152_AST);
							match(LITERAL_integer);
							break;
						}
						case LITERAL_bigint:
						{
							AST tmp153_AST = null;
							tmp153_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp153_AST);
							match(LITERAL_bigint);
							break;
						}
						case LITERAL_numeric:
						{
							AST tmp154_AST = null;
							tmp154_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp154_AST);
							match(LITERAL_numeric);
							break;
						}
						case LITERAL_text:
						{
							AST tmp155_AST = null;
							tmp155_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp155_AST);
							match(LITERAL_text);
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
					}
					else {
						if ( _cnt303>=1 ) { break _loop303; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt303++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					typecast_AST = (AST)currentAST.root;
					typecast_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPECAST,"TYPECAST")).add(typecast_AST));
					currentAST.root = typecast_AST;
					currentAST.child = typecast_AST!=null &&typecast_AST.getFirstChild()!=null ?
						typecast_AST.getFirstChild() : typecast_AST;
					currentAST.advanceChildToEnd();
				}
				typecast_AST = (AST)currentAST.root;
				returnAST = typecast_AST;
				typecastCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				typecastCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				typecastCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap fieldCache = new IdentityHashMap();
	public final void field() throws RecognitionException, TokenStreamException {
		Object retval = fieldCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST field_AST = null;
				
				{
				if ((LA(1)==IDENTIFIER) && (LA(2)==DOT)) {
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					match(DOT);
				}
				else if ((LA(1)==IDENTIFIER) && (_tokenSet_52.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				field_name();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					field_AST = (AST)currentAST.root;
					field_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD,"FIELD")).add(field_AST));
					currentAST.root = field_AST;
					currentAST.child = field_AST!=null &&field_AST.getFirstChild()!=null ?
						field_AST.getFirstChild() : field_AST;
					currentAST.advanceChildToEnd();
				}
				field_AST = (AST)currentAST.root;
				returnAST = field_AST;
				fieldCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				fieldCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				fieldCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap constantCache = new IdentityHashMap();
	public final void constant() throws RecognitionException, TokenStreamException {
		Object retval = constantCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST constant_AST = null;
				
				{
				switch ( LA(1)) {
				case QUOTED_STRING:
				{
					AST tmp157_AST = null;
					tmp157_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp157_AST);
					match(QUOTED_STRING);
					break;
				}
				case ESCAPED_STRING:
				{
					AST tmp158_AST = null;
					tmp158_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp158_AST);
					match(ESCAPED_STRING);
					break;
				}
				case INTEGER:
				{
					AST tmp159_AST = null;
					tmp159_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp159_AST);
					match(INTEGER);
					break;
				}
				case FLOAT:
				{
					AST tmp160_AST = null;
					tmp160_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp160_AST);
					match(FLOAT);
					break;
				}
				case LITERAL_true:
				{
					AST tmp161_AST = null;
					tmp161_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp161_AST);
					match(LITERAL_true);
					break;
				}
				case LITERAL_false:
				{
					AST tmp162_AST = null;
					tmp162_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp162_AST);
					match(LITERAL_false);
					break;
				}
				case LITERAL_null:
				{
					AST tmp163_AST = null;
					tmp163_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp163_AST);
					match(LITERAL_null);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					constant_AST = (AST)currentAST.root;
					constant_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTANT,"CONSTANT")).add(constant_AST));
					currentAST.root = constant_AST;
					currentAST.child = constant_AST!=null &&constant_AST.getFirstChild()!=null ?
						constant_AST.getFirstChild() : constant_AST;
					currentAST.advanceChildToEnd();
				}
				constant_AST = (AST)currentAST.root;
				returnAST = constant_AST;
				constantCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				constantCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				constantCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap safe_functionCache = new IdentityHashMap();
	public final void safe_function() throws RecognitionException, TokenStreamException {
		Object retval = safe_functionCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST safe_function_AST = null;
				
				{
				switch ( LA(1)) {
				case LITERAL_count:
				{
					AST tmp164_AST = null;
					tmp164_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp164_AST);
					match(LITERAL_count);
					match(OPEN_PAREN);
					match(ASTERISK);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_max:
				{
					AST tmp168_AST = null;
					tmp168_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp168_AST);
					match(LITERAL_max);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_min:
				{
					AST tmp171_AST = null;
					tmp171_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp171_AST);
					match(LITERAL_min);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_sum:
				{
					AST tmp174_AST = null;
					tmp174_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp174_AST);
					match(LITERAL_sum);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_avg:
				{
					AST tmp177_AST = null;
					tmp177_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp177_AST);
					match(LITERAL_avg);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_lower:
				{
					AST tmp180_AST = null;
					tmp180_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp180_AST);
					match(LITERAL_lower);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_upper:
				{
					AST tmp183_AST = null;
					tmp183_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp183_AST);
					match(LITERAL_upper);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_strpos:
				{
					AST tmp186_AST = null;
					tmp186_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp186_AST);
					match(LITERAL_strpos);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(COMMA);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_substr:
				{
					AST tmp190_AST = null;
					tmp190_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp190_AST);
					match(LITERAL_substr);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(COMMA);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case COMMA:
					{
						match(COMMA);
						abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case CLOSE_PAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_coalesce:
				{
					AST tmp195_AST = null;
					tmp195_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp195_AST);
					match(LITERAL_coalesce);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(COMMA);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_stddev:
				{
					AST tmp199_AST = null;
					tmp199_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp199_AST);
					match(LITERAL_stddev);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					safe_function_AST = (AST)currentAST.root;
					safe_function_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SAFE_FUNCTION,"SAFE_FUNCTION")).add(safe_function_AST));
					currentAST.root = safe_function_AST;
					currentAST.child = safe_function_AST!=null &&safe_function_AST.getFirstChild()!=null ?
						safe_function_AST.getFirstChild() : safe_function_AST;
					currentAST.advanceChildToEnd();
				}
				safe_function_AST = (AST)currentAST.root;
				returnAST = safe_function_AST;
				safe_functionCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				safe_functionCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				safe_functionCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap paren_valueCache = new IdentityHashMap();
	public final void paren_value() throws RecognitionException, TokenStreamException {
		Object retval = paren_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST paren_value_AST = null;
				
				match(OPEN_PAREN);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				paren_value_AST = (AST)currentAST.root;
				returnAST = paren_value_AST;
				paren_valueCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				paren_valueCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				paren_valueCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap tableCache = new IdentityHashMap();
	public final void table() throws RecognitionException, TokenStreamException {
		Object retval = tableCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST table_AST = null;
				
				table_name();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case IDENTIFIER:
				case LITERAL_as:
				{
					{
					switch ( LA(1)) {
					case LITERAL_as:
					{
						match(LITERAL_as);
						break;
					}
					case IDENTIFIER:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case SEMI:
				case LITERAL_union:
				case COMMA:
				case LITERAL_where:
				case LITERAL_group:
				case LITERAL_order:
				case LITERAL_limit:
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					table_AST = (AST)currentAST.root;
					table_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(table_AST));
					currentAST.root = table_AST;
					currentAST.child = table_AST!=null &&table_AST.getFirstChild()!=null ?
						table_AST.getFirstChild() : table_AST;
					currentAST.advanceChildToEnd();
				}
				table_AST = (AST)currentAST.root;
				returnAST = table_AST;
				tableCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				tableCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				tableCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap subqueryCache = new IdentityHashMap();
	public final void subquery() throws RecognitionException, TokenStreamException {
		Object retval = subqueryCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST subquery_AST = null;
				
				match(OPEN_PAREN);
				sql();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				{
				switch ( LA(1)) {
				case LITERAL_as:
				{
					match(LITERAL_as);
					break;
				}
				case IDENTIFIER:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				table_alias();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					subquery_AST = (AST)currentAST.root;
					subquery_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY,"SUBQUERY")).add(subquery_AST));
					currentAST.root = subquery_AST;
					currentAST.child = subquery_AST!=null &&subquery_AST.getFirstChild()!=null ?
						subquery_AST.getFirstChild() : subquery_AST;
					currentAST.advanceChildToEnd();
				}
				subquery_AST = (AST)currentAST.root;
				returnAST = subquery_AST;
				subqueryCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				subqueryCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				subqueryCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap safe_abstract_valueCache = new IdentityHashMap();
	public final void safe_abstract_value() throws RecognitionException, TokenStreamException {
		Object retval = safe_abstract_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST safe_abstract_value_AST = null;
				
				boolean synPredMatched284 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_6.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_45.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6))))) {
					int _m284 = mark();
					synPredMatched284 = true;
					inputState.guessing++;
					try {
						{
						typecast();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched284 = false;
					}
					rewind(_m284);
inputState.guessing--;
				}
				if ( synPredMatched284 ) {
					typecast();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_14.member(LA(1))) && (_tokenSet_53.member(LA(2)))) {
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==IDENTIFIER) && (_tokenSet_54.member(LA(2))) && (_tokenSet_55.member(LA(3))) && (_tokenSet_56.member(LA(4))) && (_tokenSet_35.member(LA(5))) && (_tokenSet_35.member(LA(6)))) {
					field();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if (((LA(1) >= LITERAL_count && LA(1) <= LITERAL_stddev)) && (LA(2)==OPEN_PAREN) && (_tokenSet_15.member(LA(3))) && (_tokenSet_16.member(LA(4))) && (_tokenSet_57.member(LA(5))) && (_tokenSet_46.member(LA(6)))) {
					safe_function();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_18.member(LA(3))) && (_tokenSet_57.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_35.member(LA(6)))) {
					paren_value();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = safe_abstract_value_AST;
				safe_abstract_valueCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				safe_abstract_valueCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				safe_abstract_valueCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap table_aliasCache = new IdentityHashMap();
	public final void table_alias() throws RecognitionException, TokenStreamException {
		Object retval = table_aliasCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST table_alias_AST = null;
				
				AST tmp208_AST = null;
				tmp208_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp208_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					table_alias_AST = (AST)currentAST.root;
					table_alias_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE_ALIAS,"TABLE_ALIAS")).add(table_alias_AST));
					currentAST.root = table_alias_AST;
					currentAST.child = table_alias_AST!=null &&table_alias_AST.getFirstChild()!=null ?
						table_alias_AST.getFirstChild() : table_alias_AST;
					currentAST.advanceChildToEnd();
				}
				table_alias_AST = (AST)currentAST.root;
				returnAST = table_alias_AST;
				table_aliasCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				table_aliasCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				table_aliasCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap table_nameCache = new IdentityHashMap();
	public final void table_name() throws RecognitionException, TokenStreamException {
		Object retval = table_nameCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST table_name_AST = null;
				
				AST tmp209_AST = null;
				tmp209_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp209_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					table_name_AST = (AST)currentAST.root;
					table_name_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE_NAME,"TABLE_NAME")).add(table_name_AST));
					currentAST.root = table_name_AST;
					currentAST.child = table_name_AST!=null &&table_name_AST.getFirstChild()!=null ?
						table_name_AST.getFirstChild() : table_name_AST;
					currentAST.advanceChildToEnd();
				}
				table_name_AST = (AST)currentAST.root;
				returnAST = table_name_AST;
				table_nameCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				table_nameCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				table_nameCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap field_nameCache = new IdentityHashMap();
	public final void field_name() throws RecognitionException, TokenStreamException {
		Object retval = field_nameCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST field_name_AST = null;
				
				AST tmp210_AST = null;
				tmp210_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp210_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					field_name_AST = (AST)currentAST.root;
					field_name_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD_NAME,"FIELD_NAME")).add(field_name_AST));
					currentAST.root = field_name_AST;
					currentAST.child = field_name_AST!=null &&field_name_AST.getFirstChild()!=null ?
						field_name_AST.getFirstChild() : field_name_AST;
					currentAST.advanceChildToEnd();
				}
				field_name_AST = (AST)currentAST.root;
				returnAST = field_name_AST;
				field_nameCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				field_nameCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				field_nameCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap gornull_constraintCache = new IdentityHashMap();
	public final void gornull_constraint() throws RecognitionException, TokenStreamException {
		Object retval = gornull_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST gornull_constraint_AST = null;
				Token  a = null;
				AST a_AST = null;
				Token  b = null;
				AST b_AST = null;
				AST c_AST = null;
				Token  d = null;
				AST d_AST = null;
				Token  e = null;
				AST e_AST = null;
				
				a = LT(1);
				a_AST = astFactory.create(a);
				astFactory.addASTChild(currentAST, a_AST);
				match(IDENTIFIER);
				match(DOT);
				b = LT(1);
				b_AST = astFactory.create(b);
				astFactory.addASTChild(currentAST, b_AST);
				match(IDENTIFIER);
				match(GT);
				constant();
				c_AST = (AST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
				match(LITERAL_or);
				d = LT(1);
				d_AST = astFactory.create(d);
				astFactory.addASTChild(currentAST, d_AST);
				match(IDENTIFIER);
				match(DOT);
				e = LT(1);
				e_AST = astFactory.create(e);
				astFactory.addASTChild(currentAST, e_AST);
				match(IDENTIFIER);
				match(LITERAL_is);
				match(LITERAL_null);
				if (!( a_AST.getText().equals(d_AST.getText()) && b_AST.getText().equals(e_AST.getText()) ))
				  throw new SemanticException(" a_AST.getText().equals(d_AST.getText()) && b_AST.getText().equals(e_AST.getText()) ");
				if ( inputState.guessing==0 ) {
					gornull_constraint_AST = (AST)currentAST.root;
					gornull_constraint_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(CONSTRAINT,"CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(FIELD,"FIELD")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE_ALIAS,"TABLE_ALIAS")).add(a_AST))).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD_NAME,"FIELD_NAME")).add(b_AST))))).add((AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(GORNULL,"n>")))).add(c_AST));
					currentAST.root = gornull_constraint_AST;
					currentAST.child = gornull_constraint_AST!=null &&gornull_constraint_AST.getFirstChild()!=null ?
						gornull_constraint_AST.getFirstChild() : gornull_constraint_AST;
					currentAST.advanceChildToEnd();
				}
				gornull_constraint_AST = (AST)currentAST.root;
				returnAST = gornull_constraint_AST;
				gornull_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				gornull_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				gornull_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap constraint_setCache = new IdentityHashMap();
	public final void constraint_set() throws RecognitionException, TokenStreamException {
		Object retval = constraint_setCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST constraint_set_AST = null;
				
				boolean synPredMatched348 = false;
				if (((_tokenSet_19.member(LA(1))) && (_tokenSet_20.member(LA(2))) && (_tokenSet_21.member(LA(3))) && (_tokenSet_58.member(LA(4))) && (_tokenSet_23.member(LA(5))) && (_tokenSet_24.member(LA(6))))) {
					int _m348 = mark();
					synPredMatched348 = true;
					inputState.guessing++;
					try {
						{
						safe_abstract_constraint();
						match(LITERAL_and);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched348 = false;
					}
					rewind(_m348);
inputState.guessing--;
				}
				if ( synPredMatched348 ) {
					and_constraint_set();
					astFactory.addASTChild(currentAST, returnAST);
					constraint_set_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_19.member(LA(1))) && (_tokenSet_20.member(LA(2))) && (_tokenSet_21.member(LA(3))) && (_tokenSet_59.member(LA(4))) && (_tokenSet_23.member(LA(5))) && (_tokenSet_24.member(LA(6)))) {
					or_constraint_set();
					astFactory.addASTChild(currentAST, returnAST);
					constraint_set_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = constraint_set_AST;
				constraint_setCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				constraint_setCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				constraint_setCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap safe_abstract_constraintCache = new IdentityHashMap();
	public final void safe_abstract_constraint() throws RecognitionException, TokenStreamException {
		Object retval = safe_abstract_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST safe_abstract_constraint_AST = null;
				
				switch ( LA(1)) {
				case LITERAL_not:
				{
					not_constraint();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_constraint_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_bioseg_create:
				{
					bioseg_constraint();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_constraint_AST = (AST)currentAST.root;
					break;
				}
				default:
					boolean synPredMatched333 = false;
					if (((LA(1)==OPEN_PAREN) && (_tokenSet_19.member(LA(2))) && (_tokenSet_20.member(LA(3))) && (_tokenSet_21.member(LA(4))) && (_tokenSet_22.member(LA(5))) && (_tokenSet_60.member(LA(6))))) {
						int _m333 = mark();
						synPredMatched333 = true;
						inputState.guessing++;
						try {
							{
							paren_constraint();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched333 = false;
						}
						rewind(_m333);
inputState.guessing--;
					}
					if ( synPredMatched333 ) {
						paren_constraint();
						astFactory.addASTChild(currentAST, returnAST);
						safe_abstract_constraint_AST = (AST)currentAST.root;
					}
					else {
						boolean synPredMatched335 = false;
						if (((_tokenSet_0.member(LA(1))) && (_tokenSet_61.member(LA(2))) && (_tokenSet_62.member(LA(3))) && (_tokenSet_63.member(LA(4))) && (_tokenSet_64.member(LA(5))) && (_tokenSet_65.member(LA(6))))) {
							int _m335 = mark();
							synPredMatched335 = true;
							inputState.guessing++;
							try {
								{
								subquery_constraint();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched335 = false;
							}
							rewind(_m335);
inputState.guessing--;
						}
						if ( synPredMatched335 ) {
							subquery_constraint();
							astFactory.addASTChild(currentAST, returnAST);
							safe_abstract_constraint_AST = (AST)currentAST.root;
						}
						else {
							boolean synPredMatched337 = false;
							if (((_tokenSet_0.member(LA(1))) && (_tokenSet_61.member(LA(2))) && (_tokenSet_62.member(LA(3))) && (_tokenSet_66.member(LA(4))) && (_tokenSet_66.member(LA(5))) && (_tokenSet_67.member(LA(6))))) {
								int _m337 = mark();
								synPredMatched337 = true;
								inputState.guessing++;
								try {
									{
									inlist_constraint();
									}
								}
								catch (RecognitionException pe) {
									synPredMatched337 = false;
								}
								rewind(_m337);
inputState.guessing--;
							}
							if ( synPredMatched337 ) {
								inlist_constraint();
								astFactory.addASTChild(currentAST, returnAST);
								safe_abstract_constraint_AST = (AST)currentAST.root;
							}
							else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_68.member(LA(2))) && (_tokenSet_69.member(LA(3))) && (_tokenSet_70.member(LA(4))) && (_tokenSet_71.member(LA(5))) && (_tokenSet_72.member(LA(6)))) {
								constraint();
								astFactory.addASTChild(currentAST, returnAST);
								safe_abstract_constraint_AST = (AST)currentAST.root;
							}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						}}}
						returnAST = safe_abstract_constraint_AST;
						safe_abstract_constraintCache.put(LT(1), returnAST);
					} catch (RecognitionException e) {
						safe_abstract_constraintCache.put(LT(1), e);
						throw e;
					} catch (TokenStreamException e) {
						safe_abstract_constraintCache.put(LT(1), e);
						throw e;
					}
				}
			}
			
	IdentityHashMap paren_constraintCache = new IdentityHashMap();
	public final void paren_constraint() throws RecognitionException, TokenStreamException {
		Object retval = paren_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST paren_constraint_AST = null;
				
				match(OPEN_PAREN);
				abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				paren_constraint_AST = (AST)currentAST.root;
				returnAST = paren_constraint_AST;
				paren_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				paren_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				paren_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap subquery_constraintCache = new IdentityHashMap();
	public final void subquery_constraint() throws RecognitionException, TokenStreamException {
		Object retval = subquery_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST subquery_constraint_AST = null;
				
				boolean synPredMatched357 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_73.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_74.member(LA(4))) && (_tokenSet_75.member(LA(5))) && (_tokenSet_76.member(LA(6))))) {
					int _m357 = mark();
					synPredMatched357 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(LITERAL_in);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched357 = false;
					}
					rewind(_m357);
inputState.guessing--;
				}
				if ( synPredMatched357 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_in);
					match(OPEN_PAREN);
					sql();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						subquery_constraint_AST = (AST)currentAST.root;
						subquery_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_CONSTRAINT,"SUBQUERY_CONSTRAINT")).add(subquery_constraint_AST));
						currentAST.root = subquery_constraint_AST;
						currentAST.child = subquery_constraint_AST!=null &&subquery_constraint_AST.getFirstChild()!=null ?
							subquery_constraint_AST.getFirstChild() : subquery_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					subquery_constraint_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_77.member(LA(2))) && (_tokenSet_62.member(LA(3))) && (_tokenSet_78.member(LA(4))) && (_tokenSet_63.member(LA(5))) && (_tokenSet_79.member(LA(6)))) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_not);
					match(LITERAL_in);
					match(OPEN_PAREN);
					sql();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						subquery_constraint_AST = (AST)currentAST.root;
						subquery_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_CONSTRAINT,"SUBQUERY_CONSTRAINT")).add(subquery_constraint_AST))));
						currentAST.root = subquery_constraint_AST;
						currentAST.child = subquery_constraint_AST!=null &&subquery_constraint_AST.getFirstChild()!=null ?
							subquery_constraint_AST.getFirstChild() : subquery_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					subquery_constraint_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = subquery_constraint_AST;
				subquery_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				subquery_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				subquery_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap inlist_constraintCache = new IdentityHashMap();
	public final void inlist_constraint() throws RecognitionException, TokenStreamException {
		Object retval = inlist_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST inlist_constraint_AST = null;
				
				boolean synPredMatched360 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_73.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_80.member(LA(4))) && (_tokenSet_80.member(LA(5))) && (_tokenSet_81.member(LA(6))))) {
					int _m360 = mark();
					synPredMatched360 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(LITERAL_in);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched360 = false;
					}
					rewind(_m360);
inputState.guessing--;
				}
				if ( synPredMatched360 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_in);
					match(OPEN_PAREN);
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					{
					_loop362:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							constant();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop362;
						}
						
					} while (true);
					}
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						inlist_constraint_AST = (AST)currentAST.root;
						inlist_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(INLIST_CONSTRAINT,"INLIST_CONSTRAINT")).add(inlist_constraint_AST));
						currentAST.root = inlist_constraint_AST;
						currentAST.child = inlist_constraint_AST!=null &&inlist_constraint_AST.getFirstChild()!=null ?
							inlist_constraint_AST.getFirstChild() : inlist_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					inlist_constraint_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_77.member(LA(2))) && (_tokenSet_62.member(LA(3))) && (_tokenSet_78.member(LA(4))) && (_tokenSet_66.member(LA(5))) && (_tokenSet_66.member(LA(6)))) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_not);
					match(LITERAL_in);
					match(OPEN_PAREN);
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					{
					_loop364:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							constant();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop364;
						}
						
					} while (true);
					}
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						inlist_constraint_AST = (AST)currentAST.root;
						inlist_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(INLIST_CONSTRAINT,"INLIST_CONSTRAINT")).add(inlist_constraint_AST))));
						currentAST.root = inlist_constraint_AST;
						currentAST.child = inlist_constraint_AST!=null &&inlist_constraint_AST.getFirstChild()!=null ?
							inlist_constraint_AST.getFirstChild() : inlist_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					inlist_constraint_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = inlist_constraint_AST;
				inlist_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				inlist_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				inlist_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap constraintCache = new IdentityHashMap();
	public final void constraint() throws RecognitionException, TokenStreamException {
		Object retval = constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST constraint_AST = null;
				
				boolean synPredMatched340 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_82.member(LA(2))) && (_tokenSet_83.member(LA(3))) && (_tokenSet_84.member(LA(4))) && (_tokenSet_85.member(LA(5))) && (_tokenSet_72.member(LA(6))))) {
					int _m340 = mark();
					synPredMatched340 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						comparison_op();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched340 = false;
					}
					rewind(_m340);
inputState.guessing--;
				}
				if ( synPredMatched340 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					comparison_op();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						constraint_AST = (AST)currentAST.root;
						constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTRAINT,"CONSTRAINT")).add(constraint_AST));
						currentAST.root = constraint_AST;
						currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
							constraint_AST.getFirstChild() : constraint_AST;
						currentAST.advanceChildToEnd();
					}
					constraint_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_86.member(LA(2))) && (_tokenSet_87.member(LA(3))) && (_tokenSet_88.member(LA(4))) && (_tokenSet_89.member(LA(5))) && (_tokenSet_72.member(LA(6)))) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					null_comparison();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						constraint_AST = (AST)currentAST.root;
						constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NULL_CONSTRAINT,"NULL_CONSTRAINT")).add(constraint_AST));
						currentAST.root = constraint_AST;
						currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
							constraint_AST.getFirstChild() : constraint_AST;
						currentAST.advanceChildToEnd();
					}
					constraint_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = constraint_AST;
				constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap not_constraintCache = new IdentityHashMap();
	public final void not_constraint() throws RecognitionException, TokenStreamException {
		Object retval = not_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST not_constraint_AST = null;
				
				match(LITERAL_not);
				safe_abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					not_constraint_AST = (AST)currentAST.root;
					not_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add(not_constraint_AST));
					currentAST.root = not_constraint_AST;
					currentAST.child = not_constraint_AST!=null &&not_constraint_AST.getFirstChild()!=null ?
						not_constraint_AST.getFirstChild() : not_constraint_AST;
					currentAST.advanceChildToEnd();
				}
				not_constraint_AST = (AST)currentAST.root;
				returnAST = not_constraint_AST;
				not_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				not_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				not_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap bioseg_constraintCache = new IdentityHashMap();
	public final void bioseg_constraint() throws RecognitionException, TokenStreamException {
		Object retval = bioseg_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST bioseg_constraint_AST = null;
				
				match(LITERAL_bioseg_create);
				match(OPEN_PAREN);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				match(COMMA);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				{
				switch ( LA(1)) {
				case OVERLAP:
				{
					AST tmp240_AST = null;
					tmp240_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp240_AST);
					match(OVERLAP);
					break;
				}
				case INSIDE:
				{
					AST tmp241_AST = null;
					tmp241_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp241_AST);
					match(INSIDE);
					break;
				}
				case SURROUND:
				{
					AST tmp242_AST = null;
					tmp242_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp242_AST);
					match(SURROUND);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(LITERAL_bioseg_create);
				match(OPEN_PAREN);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				match(COMMA);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				if ( inputState.guessing==0 ) {
					bioseg_constraint_AST = (AST)currentAST.root;
					bioseg_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BIOSEG_CONSTRAINT,"BIOSEG_CONSTRAINT")).add(bioseg_constraint_AST));
					currentAST.root = bioseg_constraint_AST;
					currentAST.child = bioseg_constraint_AST!=null &&bioseg_constraint_AST.getFirstChild()!=null ?
						bioseg_constraint_AST.getFirstChild() : bioseg_constraint_AST;
					currentAST.advanceChildToEnd();
				}
				bioseg_constraint_AST = (AST)currentAST.root;
				returnAST = bioseg_constraint_AST;
				bioseg_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				bioseg_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				bioseg_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap comparison_opCache = new IdentityHashMap();
	public final void comparison_op() throws RecognitionException, TokenStreamException {
		Object retval = comparison_opCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST comparison_op_AST = null;
				
				switch ( LA(1)) {
				case EQ:
				{
					AST tmp247_AST = null;
					tmp247_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp247_AST);
					match(EQ);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LT:
				{
					AST tmp248_AST = null;
					tmp248_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp248_AST);
					match(LT);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case GT:
				{
					AST tmp249_AST = null;
					tmp249_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp249_AST);
					match(GT);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case NOT_EQ:
				{
					AST tmp250_AST = null;
					tmp250_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp250_AST);
					match(NOT_EQ);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LE:
				{
					AST tmp251_AST = null;
					tmp251_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp251_AST);
					match(LE);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case GE:
				{
					AST tmp252_AST = null;
					tmp252_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp252_AST);
					match(GE);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case GORNULL:
				{
					AST tmp253_AST = null;
					tmp253_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp253_AST);
					match(GORNULL);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_not:
				{
					AST tmp254_AST = null;
					tmp254_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp254_AST);
					match(LITERAL_not);
					AST tmp255_AST = null;
					tmp255_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp255_AST);
					match(LITERAL_like);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_like:
				{
					AST tmp256_AST = null;
					tmp256_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp256_AST);
					match(LITERAL_like);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = comparison_op_AST;
				comparison_opCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				comparison_opCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				comparison_opCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap null_comparisonCache = new IdentityHashMap();
	public final void null_comparison() throws RecognitionException, TokenStreamException {
		Object retval = null_comparisonCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST null_comparison_AST = null;
				
				if ((LA(1)==LITERAL_is) && (LA(2)==LITERAL_null)) {
					match(LITERAL_is);
					match(LITERAL_null);
					null_comparison_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==LITERAL_is) && (LA(2)==LITERAL_not)) {
					match(LITERAL_is);
					AST tmp260_AST = null;
					tmp260_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp260_AST);
					match(LITERAL_not);
					match(LITERAL_null);
					null_comparison_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = null_comparison_AST;
				null_comparisonCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				null_comparisonCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				null_comparisonCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap and_constraint_setCache = new IdentityHashMap();
	public final void and_constraint_set() throws RecognitionException, TokenStreamException {
		Object retval = and_constraint_setCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST and_constraint_set_AST = null;
				
				safe_abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				{
				int _cnt354=0;
				_loop354:
				do {
					if ((LA(1)==LITERAL_and)) {
						match(LITERAL_and);
						safe_abstract_constraint();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						if ( _cnt354>=1 ) { break _loop354; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt354++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					and_constraint_set_AST = (AST)currentAST.root;
					and_constraint_set_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(AND_CONSTRAINT_SET,"AND_CONSTRAINT_SET")).add(and_constraint_set_AST));
					currentAST.root = and_constraint_set_AST;
					currentAST.child = and_constraint_set_AST!=null &&and_constraint_set_AST.getFirstChild()!=null ?
						and_constraint_set_AST.getFirstChild() : and_constraint_set_AST;
					currentAST.advanceChildToEnd();
				}
				and_constraint_set_AST = (AST)currentAST.root;
				returnAST = and_constraint_set_AST;
				and_constraint_setCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				and_constraint_setCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				and_constraint_setCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap or_constraint_setCache = new IdentityHashMap();
	public final void or_constraint_set() throws RecognitionException, TokenStreamException {
		Object retval = or_constraint_setCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST or_constraint_set_AST = null;
				
				safe_abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				{
				int _cnt351=0;
				_loop351:
				do {
					if ((LA(1)==LITERAL_or)) {
						match(LITERAL_or);
						safe_abstract_constraint();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						if ( _cnt351>=1 ) { break _loop351; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt351++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					or_constraint_set_AST = (AST)currentAST.root;
					or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OR_CONSTRAINT_SET,"OR_CONSTRAINT_SET")).add(or_constraint_set_AST));
					currentAST.root = or_constraint_set_AST;
					currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
						or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
					currentAST.advanceChildToEnd();
				}
				or_constraint_set_AST = (AST)currentAST.root;
				returnAST = or_constraint_set_AST;
				or_constraint_setCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				or_constraint_setCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				or_constraint_setCache.put(LT(1), e);
				throw e;
			}
		}
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
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 9218877181780819968L, 536870912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -4494855073955840L, 6979321887L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { -8847632629760L, 8053063711L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { -8847632629760L, 16777478175L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { -8813272891392L, 16643260447L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { -8847632629758L, 16778051615L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 9218877181780819968L, 6979321856L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { -8813272891392L, 16777478175L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { -8813272891390L, 16778051615L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 2L, 3356278784L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 9218877220435525634L, 1118488870912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { -4494842189053950L, 1193656959519L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { -8800387989502L, 3427107078143L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { -8800387989502L, 3435973836799L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 8675833937920L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 9218877181780819968L, 536870914L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { -4494855073955840L, 8053325855L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { -8847632629760L, 8187281439L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { -4494855073955840L, 8053063711L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 9218877181780819968L, 69256348160L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { -4494855073955840L, 2309081808895L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { -8847632629760L, 2310155550719L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { -8843337662464L, 3435437309951L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { -8834747727872L, 3435437441023L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { -8834747727872L, 3435572215807L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { -8843337662462L, 2318798290943L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { -8834747727870L, 3435628331007L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { -8834747727870L, 3435697012735L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { -8847632629758L, 3367047069695L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { -8843337662462L, 4397977698303L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { -8800387989502L, 4398045986815L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { 2L, 3351477813216L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 2L, 3353625296864L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { -4494850778988542L, 4387240280063L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { -8800387989502L, 4389456052223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { -8800387989502L, 4398046511103L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { -8847632629758L, 3358457135103L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { -4494855073955840L, 7247757343L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { -8847632629758L, 8086929439L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { -8843337662462L, 1133789954079L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { -8834747727870L, 1202581914143L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { -8834747727870L, 3435965448191L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { -4494855073955838L, 8086929439L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { -8843337662462L, 1124931321887L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { -4494855073955840L, 6979321856L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { -8847632629758L, 3367181287423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { -8808977924094L, 4397977698303L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 9218877181780819968L, 6979321857L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 9218877181780819968L, 6979321858L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { 9218877181780819968L, 6979321860L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 9218877181780819968L, 6979321864L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 9218877181780819968L, 6979321872L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { -9223372036854775806L, 3355907522559L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { -9223372036854775806L, 3351612030975L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { -9223372036854775806L, 3353759514623L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { -4494816419250174L, 4387240280063L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { -8800387989502L, 4389456576511L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { -8847632629758L, 3358591352831L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { -8843337662464L, 3418257440767L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { -8843337662464L, 2335925682175L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { -8834747727870L, 3435489918975L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { -4494855073955840L, 2206002577951L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { -8847632629760L, 2207076319263L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { -8843337662464L, 2215666582047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { -8834747727872L, 2215666713119L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { -8834747727872L, 2215801487903L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { -8847632629760L, 2215666516511L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { -8847632629758L, 3332410491423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { -4494855073955840L, 41339076607L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { -8847632629760L, 8053065247L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { -8847632629758L, 1167746990079L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { -8843337662462L, 1236604944383L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { -8834747727870L, 3435696488447L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = { -4494855073955840L, 2206002577439L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = { -8843337662464L, 2215666581535L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = { -8839042695168L, 2215666712607L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	private static final long[] mk_tokenSet_76() {
		long[] data = { -8834747727872L, 2215801487391L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_76 = new BitSet(mk_tokenSet_76());
	private static final long[] mk_tokenSet_77() {
		long[] data = { -4494855073955840L, 6979322399L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_77 = new BitSet(mk_tokenSet_77());
	private static final long[] mk_tokenSet_78() {
		long[] data = { -8847632629760L, 16643260959L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_78 = new BitSet(mk_tokenSet_78());
	private static final long[] mk_tokenSet_79() {
		long[] data = { -8839042695168L, 2215666713119L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_79 = new BitSet(mk_tokenSet_79());
	private static final long[] mk_tokenSet_80() {
		long[] data = { -8847632629760L, 2215666515999L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_80 = new BitSet(mk_tokenSet_80());
	private static final long[] mk_tokenSet_81() {
		long[] data = { -8847632629758L, 3332410490911L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_81 = new BitSet(mk_tokenSet_81());
	private static final long[] mk_tokenSet_82() {
		long[] data = { -4494855073955840L, 6979338239L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_82 = new BitSet(mk_tokenSet_82());
	private static final long[] mk_tokenSet_83() {
		long[] data = { -8847632629760L, 8053064735L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_83 = new BitSet(mk_tokenSet_83());
	private static final long[] mk_tokenSet_84() {
		long[] data = { -8847632629758L, 1133387251711L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_84 = new BitSet(mk_tokenSet_84());
	private static final long[] mk_tokenSet_85() {
		long[] data = { -8843337662462L, 1202245206015L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_85 = new BitSet(mk_tokenSet_85());
	private static final long[] mk_tokenSet_86() {
		long[] data = { -4494855073955840L, 41339060255L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_86 = new BitSet(mk_tokenSet_86());
	private static final long[] mk_tokenSet_87() {
		long[] data = { -8847632629760L, 8053064223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_87 = new BitSet(mk_tokenSet_87());
	private static final long[] mk_tokenSet_88() {
		long[] data = { -8847632629758L, 1167746973727L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_88 = new BitSet(mk_tokenSet_88());
	private static final long[] mk_tokenSet_89() {
		long[] data = { -8843337662462L, 1236604928543L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_89 = new BitSet(mk_tokenSet_89());
	
	}
