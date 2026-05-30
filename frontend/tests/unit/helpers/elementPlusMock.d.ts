declare module '../../helpers/elementPlusMock' {
  export const elementPlusStubs: Record<string, any>;
  export const lucideIconsStub: Record<string, any>;
  export const getAllStubs: () => Record<string, any>;
  export const routerMock: {
    push: (...args: any[]) => any;
    replace: (...args: any[]) => any;
    back: (...args: any[]) => any;
    forward: (...args: any[]) => any;
    currentRoute: { value: { path: string; name: string; params: Record<string, any> } };
  };
  export const routeMock: {
    path: string;
    name: string;
    params: Record<string, any>;
    query: Record<string, any>;
    meta: Record<string, any>;
  };
}
