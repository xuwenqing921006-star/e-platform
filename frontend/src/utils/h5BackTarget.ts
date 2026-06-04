import type { LocationQueryRaw } from 'vue-router'

import type { CountyCode, PublicScope } from '../types/api'

type H5BackState = {
  primaryTab: PublicScope
  financialTab: string
  countyCode: CountyCode
  ruralSection: string
}

export function buildH5DetailBackQuery(state: H5BackState): LocationQueryRaw {
  return {
    from_primary: state.primaryTab,
    from_financial: state.financialTab,
    from_county: state.countyCode,
    from_rural_section: state.ruralSection,
  }
}

export function resolveH5BackTarget(query: Record<string, unknown>) {
  const params = new URLSearchParams()
  const primary = firstQueryValue(query.from_primary)
  const financial = firstQueryValue(query.from_financial)
  const county = firstQueryValue(query.from_county)
  const ruralSection = firstQueryValue(query.from_rural_section)

  if (primary) params.set('primary', primary)
  if (financial) params.set('financial', financial)
  if (county) params.set('county', county)
  if (ruralSection) params.set('rural_section', ruralSection)

  const serialized = params.toString()
  return serialized ? `/h5/?${serialized}` : '/h5/'
}

function firstQueryValue(value: unknown) {
  return Array.isArray(value) ? String(value[0] || '') : String(value || '')
}
