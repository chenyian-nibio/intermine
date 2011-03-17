// $ANTLR 2.7.6 (20060327): "intermine_sql.g" -> "SqlLexer.java"$

package org.intermine.sql.query;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class SqlLexer extends antlr.CharScanner implements SqlTokenTypes, TokenStream
 {
public SqlLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public SqlLexer(Reader in) {
	this(new CharBuffer(in));
}
public SqlLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public SqlLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = false;
	setCaseSensitive(false);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("all", this), new Integer(81));
	literals.put(new ANTLRHashString("count", this), new Integer(52));
	literals.put(new ANTLRHashString("sum", this), new Integer(55));
	literals.put(new ANTLRHashString("min", this), new Integer(54));
	literals.put(new ANTLRHashString("lower", this), new Integer(57));
	literals.put(new ANTLRHashString("upper", this), new Integer(58));
	literals.put(new ANTLRHashString("smallint", this), new Integer(47));
	literals.put(new ANTLRHashString("false", this), new Integer(41));
	literals.put(new ANTLRHashString("true", this), new Integer(40));
	literals.put(new ANTLRHashString("integer", this), new Integer(48));
	literals.put(new ANTLRHashString("and", this), new Integer(104));
	literals.put(new ANTLRHashString("desc", this), new Integer(92));
	literals.put(new ANTLRHashString("select", this), new Integer(80));
	literals.put(new ANTLRHashString("distinct", this), new Integer(33));
	literals.put(new ANTLRHashString("text", this), new Integer(51));
	literals.put(new ANTLRHashString("boolean", this), new Integer(44));
	literals.put(new ANTLRHashString("group", this), new Integer(85));
	literals.put(new ANTLRHashString("where", this), new Integer(84));
	literals.put(new ANTLRHashString("precision", this), new Integer(97));
	literals.put(new ANTLRHashString("avg", this), new Integer(56));
	literals.put(new ANTLRHashString("order", this), new Integer(88));
	literals.put(new ANTLRHashString("limit", this), new Integer(89));
	literals.put(new ANTLRHashString("in", this), new Integer(105));
	literals.put(new ANTLRHashString("null", this), new Integer(42));
	literals.put(new ANTLRHashString("strpos", this), new Integer(59));
	literals.put(new ANTLRHashString("offset", this), new Integer(90));
	literals.put(new ANTLRHashString("bigint", this), new Integer(49));
	literals.put(new ANTLRHashString("having", this), new Integer(87));
	literals.put(new ANTLRHashString("union", this), new Integer(79));
	literals.put(new ANTLRHashString("or", this), new Integer(98));
	literals.put(new ANTLRHashString("stddev", this), new Integer(62));
	literals.put(new ANTLRHashString("from", this), new Integer(83));
	literals.put(new ANTLRHashString("real", this), new Integer(45));
	literals.put(new ANTLRHashString("max", this), new Integer(53));
	literals.put(new ANTLRHashString("is", this), new Integer(99));
	literals.put(new ANTLRHashString("like", this), new Integer(74));
	literals.put(new ANTLRHashString("numeric", this), new Integer(50));
	literals.put(new ANTLRHashString("bioseg_create", this), new Integer(100));
	literals.put(new ANTLRHashString("substr", this), new Integer(60));
	literals.put(new ANTLRHashString("double", this), new Integer(46));
	literals.put(new ANTLRHashString("not", this), new Integer(73));
	literals.put(new ANTLRHashString("explain", this), new Integer(32));
	literals.put(new ANTLRHashString("coalesce", this), new Integer(61));
	literals.put(new ANTLRHashString("by", this), new Integer(86));
	literals.put(new ANTLRHashString("as", this), new Integer(91));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '"':
				{
					mALIAS(true);
					theRetToken=_returnToken;
					break;
				}
				case '\'':
				{
					mQUOTED_STRING(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case '.':
				{
					mDOT(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '*':
				{
					mASTERISK(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mOPEN_PAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mCLOSE_PAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '&':
				{
					mOVERLAP(true);
					theRetToken=_returnToken;
					break;
				}
				case '+':
				{
					mPLUS(true);
					theRetToken=_returnToken;
					break;
				}
				case '-':
				{
					mMINUS(true);
					theRetToken=_returnToken;
					break;
				}
				case '/':
				{
					mDIVIDE(true);
					theRetToken=_returnToken;
					break;
				}
				case '%':
				{
					mPERCENT(true);
					theRetToken=_returnToken;
					break;
				}
				case '|':
				{
					mVERTBAR(true);
					theRetToken=_returnToken;
					break;
				}
				case ':':
				{
					mCOLONTYPE(true);
					theRetToken=_returnToken;
					break;
				}
				case '=':
				{
					mEQ(true);
					theRetToken=_returnToken;
					break;
				}
				case '>':
				{
					mGT(true);
					theRetToken=_returnToken;
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mFLOAT(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\r':  case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='e') && (LA(2)=='\'')) {
						mESCAPED_STRING(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='@')) {
						mINSIDE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='@') && (LA(2)=='>')) {
						mSURROUND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='n') && (LA(2)=='>')) {
						mGORNULL(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1) >= 'a' && LA(1) <= 'z')) && (true)) {
						mIDENTIFIER(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='@') && (true)) {
						mAT_SIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!'||LA(1)=='<'||LA(1)=='^') && (true)) {
						mNOT_EQ(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mIDENTIFIER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENTIFIER;
		int _saveIndex;
		
		matchRange('a','z');
		{
		_loop369:
		do {
			switch ( LA(1)) {
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				matchRange('a','z');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				matchRange('0','9');
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			case '#':
			{
				match('#');
				break;
			}
			default:
			{
				break _loop369;
			}
			}
		} while (true);
		}
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mALIAS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ALIAS;
		int _saveIndex;
		
		match('"');
		matchRange('a','z');
		{
		_loop372:
		do {
			switch ( LA(1)) {
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				matchRange('a','z');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				matchRange('0','9');
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			case '#':
			{
				match('#');
				break;
			}
			default:
			{
				break _loop372;
			}
			}
		} while (true);
		}
		match('"');
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQUOTED_STRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUOTED_STRING;
		int _saveIndex;
		
		match('\'');
		{
		_loop375:
		do {
			if ((_tokenSet_0.member(LA(1)))) {
				matchNot('\'');
			}
			else {
				break _loop375;
			}
			
		} while (true);
		}
		{
		_loop379:
		do {
			if ((LA(1)=='\'') && (LA(2)=='\'')) {
				match('\'');
				match('\'');
				{
				_loop378:
				do {
					if ((_tokenSet_0.member(LA(1)))) {
						matchNot('\'');
					}
					else {
						break _loop378;
					}
					
				} while (true);
				}
			}
			else {
				break _loop379;
			}
			
		} while (true);
		}
		match('\'');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mESCAPED_STRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESCAPED_STRING;
		int _saveIndex;
		
		match('e');
		match('\'');
		{
		_loop383:
		do {
			if ((_tokenSet_1.member(LA(1)))) {
				{
				match(_tokenSet_1);
				}
			}
			else {
				break _loop383;
			}
			
		} while (true);
		}
		{
		_loop389:
		do {
			if ((LA(1)=='\\')) {
				match('\\');
				{
				switch ( LA(1)) {
				case '\'':
				{
					match('\'');
					break;
				}
				case '\\':
				{
					match('\\');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				_loop388:
				do {
					if ((_tokenSet_1.member(LA(1)))) {
						{
						match(_tokenSet_1);
						}
					}
					else {
						break _loop388;
					}
					
				} while (true);
				}
			}
			else {
				break _loop389;
			}
			
		} while (true);
		}
		match('\'');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mASTERISK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ASTERISK;
		int _saveIndex;
		
		match('*');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT_SIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT_SIGN;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mOPEN_PAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OPEN_PAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCLOSE_PAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CLOSE_PAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mOVERLAP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OVERLAP;
		int _saveIndex;
		
		match("&&");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINSIDE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INSIDE;
		int _saveIndex;
		
		match("<@");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSURROUND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SURROUND;
		int _saveIndex;
		
		match("@>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		boolean synPredMatched405 = false;
		if (((LA(1)=='-') && ((LA(2) >= '0' && LA(2) <= '9')))) {
			int _m405 = mark();
			synPredMatched405 = true;
			inputState.guessing++;
			try {
				{
				match('-');
				{
				int _cnt404=0;
				_loop404:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt404>=1 ) { break _loop404; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt404++;
				} while (true);
				}
				match('.');
				matchRange('0','9');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched405 = false;
			}
			rewind(_m405);
inputState.guessing--;
		}
		if ( synPredMatched405 ) {
			match('-');
			{
			int _cnt407=0;
			_loop407:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt407>=1 ) { break _loop407; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt407++;
			} while (true);
			}
			match('.');
			{
			int _cnt409=0;
			_loop409:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt409>=1 ) { break _loop409; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt409++;
			} while (true);
			}
			{
			if ((LA(1)=='e')) {
				match('e');
				{
				switch ( LA(1)) {
				case '-':
				{
					match('-');
					break;
				}
				case '+':
				{
					match('+');
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				int _cnt413=0;
				_loop413:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt413>=1 ) { break _loop413; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt413++;
				} while (true);
				}
			}
			else {
			}
			
			}
			{
			if ((LA(1)==':')) {
				match("::real");
			}
			else {
			}
			
			}
			if ( inputState.guessing==0 ) {
				_ttype = FLOAT;
			}
		}
		else {
			boolean synPredMatched416 = false;
			if (((LA(1)=='-') && ((LA(2) >= '0' && LA(2) <= '9')))) {
				int _m416 = mark();
				synPredMatched416 = true;
				inputState.guessing++;
				try {
					{
					match('-');
					matchRange('0','9');
					}
				}
				catch (RecognitionException pe) {
					synPredMatched416 = false;
				}
				rewind(_m416);
inputState.guessing--;
			}
			if ( synPredMatched416 ) {
				match('-');
				{
				int _cnt418=0;
				_loop418:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt418>=1 ) { break _loop418; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt418++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					_ttype = INTEGER;
				}
			}
			else if ((LA(1)=='-') && (true)) {
				match('-');
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	public final void mDIVIDE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIVIDE;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPERCENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PERCENT;
		int _saveIndex;
		
		match('%');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mVERTBAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = VERTBAR;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLONTYPE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLONTYPE;
		int _saveIndex;
		
		match("::");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQ(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQ;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQ(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_EQ;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '<':
		{
			match('<');
			if ( inputState.guessing==0 ) {
				_ttype = LT;
			}
			{
			switch ( LA(1)) {
			case '>':
			{
				{
				match('>');
				if ( inputState.guessing==0 ) {
					_ttype = NOT_EQ;
				}
				}
				break;
			}
			case '=':
			{
				{
				match('=');
				if ( inputState.guessing==0 ) {
					_ttype = LE;
				}
				}
				break;
			}
			default:
				{
				}
			}
			}
			break;
		}
		case '!':
		{
			match("!=");
			break;
		}
		case '^':
		{
			match("^=");
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GT;
		int _saveIndex;
		
		match('>');
		{
		if ((LA(1)=='=')) {
			match('=');
			if ( inputState.guessing==0 ) {
				_ttype = GE;
			}
		}
		else {
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGORNULL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GORNULL;
		int _saveIndex;
		
		match("n>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mFLOAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT;
		int _saveIndex;
		
		boolean synPredMatched435 = false;
		if ((((LA(1) >= '0' && LA(1) <= '9')) && (_tokenSet_2.member(LA(2))))) {
			int _m435 = mark();
			synPredMatched435 = true;
			inputState.guessing++;
			try {
				{
				{
				int _cnt434=0;
				_loop434:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt434>=1 ) { break _loop434; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt434++;
				} while (true);
				}
				match('.');
				matchRange('0','9');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched435 = false;
			}
			rewind(_m435);
inputState.guessing--;
		}
		if ( synPredMatched435 ) {
			{
			int _cnt437=0;
			_loop437:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt437>=1 ) { break _loop437; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt437++;
			} while (true);
			}
			match('.');
			{
			int _cnt439=0;
			_loop439:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt439>=1 ) { break _loop439; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt439++;
			} while (true);
			}
			{
			if ((LA(1)=='e')) {
				match('e');
				{
				switch ( LA(1)) {
				case '-':
				{
					match('-');
					break;
				}
				case '+':
				{
					match('+');
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				int _cnt443=0;
				_loop443:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt443>=1 ) { break _loop443; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt443++;
				} while (true);
				}
			}
			else {
			}
			
			}
			{
			if ((LA(1)==':')) {
				match("::real");
			}
			else {
			}
			
			}
		}
		else if (((LA(1) >= '0' && LA(1) <= '9')) && (true)) {
			{
			int _cnt446=0;
			_loop446:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt446>=1 ) { break _loop446; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt446++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				_ttype = INTEGER;
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case ' ':
		{
			match(' ');
			break;
		}
		case '\t':
		{
			match('\t');
			break;
		}
		case '\n':
		{
			match('\n');
			if ( inputState.guessing==0 ) {
				newline();
			}
			break;
		}
		default:
			if ((LA(1)=='\r') && (LA(2)=='\n')) {
				match('\r');
				match('\n');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if ((LA(1)=='\r') && (true)) {
				match('\r');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -549755813896L, -1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -549755813896L, -268435457L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 288019269919178752L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
