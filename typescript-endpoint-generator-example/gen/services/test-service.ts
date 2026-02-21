import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {lastValueFrom} from 'rxjs';

import {Class1} from '../dto/class1';
import {Class2} from '../dto/class2';
import {TestId} from '../dto/test-id';
import {Void} from '../dto/../../dto/custom-void';

@Injectable({
  providedIn: 'root',
})
export class TestService<U> {
  private readonly httpClient = inject(HttpClient);
  private readonly basePath = 'test/zxczxc';

  public async method1<T>(a2: number[], c: number, f: number[], f1: number[][], g: Record<string, number>, h: Class1, h1: Class2<U, T>, h2: Class2<U, Class2<Class1, T>>, i: T[], j: TestId, k: TestId, v: Void, a?: number, a1?: boolean, b?: number, d?: string = 'qwe', e?: number = 1.23, u?: U): Promise<string> {
    const notSoSimple = 'a';
    const httpParams: Record<string, any> = {
      notSoSimple2: a1,
      a2: a2,
      notSoSimple3: b,
      c: c,
      d: d,
      e: e,
      f: f,
      f1: f1,
      g: g,
      h: h,
      h1: h1,
      h2: h2,
      i: i,
      j: j,
      k: k,
      v: v
    };
    return lastValueFrom(this.httpClient.request<string>('GET', `${this.basePath}/method1/{notSoSimple}`, {
      params: httpParams,
      body: u
    }));
  }

  public async method2(): Promise<Class2<Class1, Class2<number, string>>> {
    return lastValueFrom(this.httpClient.request<Class2<Class1, Class2<number, string>>>('POST', `${this.basePath}/method2`, {
    }));
  }

  public async method3(): Promise<Blob> {
    return lastValueFrom(this.httpClient.request<Blob>('POST', `${this.basePath}/res`, {
    }));
  }

}
