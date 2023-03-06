import {Injectable} from '@angular/core';
import {CurrentUserDto} from '@api';

@Injectable({
  providedIn: 'root'
})
export class AuthStore {
  private _user?: CurrentUserDto;

  public reset(): void {
    this.setCurrentUser();
    this.setAuthToken();
  }

  public setCurrentUser(user?: CurrentUserDto): void {
    this._user = Object.freeze(user);
  }

  public getCurrentUser(): CurrentUserDto | undefined {
    return this._user;
  }

  public getAuthToken(): string | null {
    return localStorage.getItem('token');
  }

  public setAuthToken(token?: string): void {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }

}
