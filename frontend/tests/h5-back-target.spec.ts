import { describe, expect, it } from 'vitest'

import {
  buildH5DetailBackQuery,
  resolveH5BackTarget,
} from '../src/utils/h5BackTarget'

describe('h5 detail back target', () => {
  it('keeps the current list tab as detail back target', () => {
    expect(
      buildH5DetailBackQuery({
        primaryTab: 'FINANCIAL',
        financialTab: 'POLICY_PROMOTION',
        countyCode: 'DUMENG',
        ruralSection: 'SERVICE_GUIDE',
      }),
    ).toEqual({
      from_primary: 'FINANCIAL',
      from_financial: 'POLICY_PROMOTION',
      from_county: 'DUMENG',
      from_rural_section: 'SERVICE_GUIDE',
    })
  })

  it('uses query context before falling back to h5 home', () => {
    expect(
      resolveH5BackTarget({
        from_primary: 'FINANCIAL',
        from_financial: 'PRODUCTS',
      }),
    ).toBe('/h5/?primary=FINANCIAL&financial=PRODUCTS')
    expect(resolveH5BackTarget({})).toBe('/h5/')
  })
})
