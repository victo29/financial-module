import { FormControl } from '@angular/forms';
import { cpfValidator } from './cpf.validator';

describe('cpfValidator', () => {

  // ─── VALID CPF ───────────────────────────────────────────────────────────

  describe('given a mathematically valid CPF in formatted notation', () => {
    it('when the validator runs, then it should return null (no error)', () => {
      const control = new FormControl('529.982.247-25');
      const result = cpfValidator(control);
      expect(result).toBeNull();
    });
  });

  describe('given another mathematically valid CPF', () => {
    it('when the validator runs, then it should return null (no error)', () => {
      const control = new FormControl('123.456.789-09');
      const result = cpfValidator(control);
      expect(result).toBeNull();
    });
  });

  // ─── INVALID — REPEATED DIGITS ───────────────────────────────────────────

  describe('given a CPF composed entirely of repeated digits (111.111.111-11)', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('111.111.111-11');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  describe('given CPF 000.000.000-00 (all zeros)', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('000.000.000-00');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  describe('given CPF 999.999.999-99 (all nines)', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('999.999.999-99');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  // ─── INVALID — WRONG CHECK DIGITS ────────────────────────────────────────

  describe('given a CPF with an incorrect first check digit', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('529.982.247-35');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  describe('given a CPF with an incorrect second check digit', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('529.982.247-26');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  // ─── INVALID — WRONG LENGTH ───────────────────────────────────────────────

  describe('given a CPF with fewer than 11 digits', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('123.456.789');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  describe('given a CPF with more than 11 digits', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('123.456.789-099');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  // ─── INVALID — EMPTY / NULL ───────────────────────────────────────────────

  describe('given an empty string value', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl('');
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  describe('given a null value', () => {
    it('when the validator runs, then it should return { cpfInvalid: true }', () => {
      const control = new FormControl(null);
      const result = cpfValidator(control);
      expect(result).toEqual({ cpfInvalid: true });
    });
  });

  // ─── DIGITS ONLY (NO MASK) ────────────────────────────────────────────────

  describe('given a valid CPF without formatting (digits only)', () => {
    it('when the validator runs, then it should return null (no error)', () => {
      const control = new FormControl('52998224725');
      const result = cpfValidator(control);
      expect(result).toBeNull();
    });
  });
});
