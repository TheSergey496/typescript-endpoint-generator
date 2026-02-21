// @ts-nocheck
import {Class2} from 'class2';
import {Enumeration} from 'enumeration';
import {Void} from '../../dto/custom-void';

export class Class1 {
	a: number;
	d: number;
	b: number;
	c: Void;
	e: number;
	f: Class1;
	g: Class2<Enumeration, Class2<Class1, number>>;
	enumeration: Enumeration;
}
