const Enumeration = {
	Variant1: 'VARIANT1',
	Variant2: 'VARIANT2'
} as const;

export type Enumeration = typeof Enumeration[keyof typeof Enumeration];
